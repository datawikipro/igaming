package com.antigravity.overlay.service;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class OsCredentialManager {

    private static final Logger log = LoggerFactory.getLogger(OsCredentialManager.class);

    private static final String TARGET_NAME = "gemini:antigravity";
    private static final int CRED_TYPE_GENERIC = 1;
    
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private static final Path LINUX_TOKEN_FILE =
            Paths.get(System.getProperty("user.home"), ".gemini", "antigravity-cli", "antigravity-oauth-token");

    // ── JNA structure matching Windows API CREDENTIALW ───────────────────────

    public static class CREDENTIALW extends Structure {
        public int Flags;
        public int Type;
        public WString TargetName;
        public WString Comment;
        public FILETIME LastWritten;
        public int CredentialBlobSize;
        public Pointer CredentialBlob;
        public int Persist;
        public int AttributeCount;
        public Pointer Attributes;
        public WString TargetAlias;
        public WString UserName;

        public CREDENTIALW() {}

        public CREDENTIALW(Pointer p) {
            super(p);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                "Flags", "Type", "TargetName", "Comment", "LastWritten",
                "CredentialBlobSize", "CredentialBlob", "Persist", "AttributeCount",
                "Attributes", "TargetAlias", "UserName"
            );
        }
    }

    // ── JNA mapping to advapi32.dll ──────────────────────────────────────────

    public interface Advapi32 extends StdCallLibrary {
        Advapi32 INSTANCE = Native.load("advapi32", Advapi32.class, W32APIOptions.UNICODE_OPTIONS);

        boolean CredReadW(WString TargetName, int Type, int Flags, PointerByReference pCredential);
        boolean CredWriteW(CREDENTIALW Credential, int Flags);
        boolean CredDeleteW(WString TargetName, int Type, int Flags);
        void CredFree(Pointer Buffer);
    }

    // ── Backup Record ────────────────────────────────────────────────────────

    public record CredentialBackup(
        String userName,
        byte[] blob
    ) {}

    // ── Public API ───────────────────────────────────────────────────────────

    public static CredentialBackup backupAndClear() {
        if (!IS_WINDOWS) {
            log.info("[OsCred] Linux backup: backing up fallback token file...");
            try {
                byte[] blob = new byte[0];
                if (Files.exists(LINUX_TOKEN_FILE)) {
                    blob = Files.readAllBytes(LINUX_TOKEN_FILE);
                    Files.delete(LINUX_TOKEN_FILE);
                    log.info("[OsCred] Backed up and deleted Linux fallback token file.");
                } else {
                    log.info("[OsCred] No Linux fallback token file found to backup.");
                }
                return new CredentialBackup("linux-fallback", blob);
            } catch (Exception e) {
                log.error("[OsCred] Failed to backup Linux fallback token file: ", e);
                return new CredentialBackup("linux-fallback", new byte[0]);
            }
        }
        
        try {
            PointerByReference pRef = new PointerByReference();
            WString target = new WString(TARGET_NAME);

            log.info("[WinCred] Reading existing credential for '{}'...", TARGET_NAME);
            boolean success = Advapi32.INSTANCE.CredReadW(target, CRED_TYPE_GENERIC, 0, pRef);

            CredentialBackup backup = null;
            if (success) {
                Pointer p = pRef.getValue();
                try {
                    CREDENTIALW cred = new CREDENTIALW(p);
                    String userName = cred.UserName != null ? cred.UserName.toString() : "antigravity";
                    byte[] blob = new byte[cred.CredentialBlobSize];
                    if (cred.CredentialBlobSize > 0 && cred.CredentialBlob != null) {
                        cred.CredentialBlob.read(0, blob, 0, cred.CredentialBlobSize);
                    }
                    backup = new CredentialBackup(userName, blob);
                    log.info("[WinCred] Successfully backed up credential for user '{}'.", userName);
                } finally {
                    Advapi32.INSTANCE.CredFree(p);
                }

                log.info("[WinCred] Deleting credential from Credential Manager...");
                Advapi32.INSTANCE.CredDeleteW(target, CRED_TYPE_GENERIC, 0);
            } else {
                log.info("[WinCred] No credential found for '{}' to backup.", TARGET_NAME);
            }
            return backup;
        } catch (Throwable e) {
            log.error("[WinCred] Failed to backup credential: ", e);
            return null;
        }
    }

    public static void restore(CredentialBackup backup) {
        if (!IS_WINDOWS) {
            log.info("[OsCred] Linux restore: restoring fallback token file...");
            if (backup == null || !"linux-fallback".equals(backup.userName())) {
                log.warn("[OsCred] Invalid backup record for Linux restore.");
                return;
            }
            try {
                if (backup.blob().length > 0) {
                    Files.createDirectories(LINUX_TOKEN_FILE.getParent());
                    Files.write(LINUX_TOKEN_FILE, backup.blob());
                    log.info("[OsCred] Restored Linux fallback token file.");
                } else {
                    Files.deleteIfExists(LINUX_TOKEN_FILE);
                    log.info("[OsCred] Cleared Linux fallback token file (was empty).");
                }
            } catch (Exception e) {
                log.error("[OsCred] Failed to restore Linux fallback token file: ", e);
            }
            return;
        }
        
        if (backup == null || backup.blob().length == 0) {
            try {
                log.info("[WinCred] No backup to restore. Ensuring target '{}' is cleared...", TARGET_NAME);
                Advapi32.INSTANCE.CredDeleteW(new WString(TARGET_NAME), CRED_TYPE_GENERIC, 0);
            } catch (Throwable e) {
                log.error("[WinCred] Failed to clear credential: ", e);
            }
            return;
        }

        try {
            log.info("[WinCred] Restoring credential for user '{}'...", backup.userName());

            CREDENTIALW cred = new CREDENTIALW();
            cred.Flags = 0;
            cred.Type = CRED_TYPE_GENERIC;
            cred.TargetName = new WString(TARGET_NAME);
            cred.Persist = 2; // CRED_PERSIST_LOCAL_MACHINE
            cred.UserName = new WString(backup.userName());

            byte[] blob = backup.blob();
            cred.CredentialBlobSize = blob.length;
            if (blob.length > 0) {
                Pointer pBlob = new com.sun.jna.Memory(blob.length);
                pBlob.write(0, blob, 0, blob.length);
                cred.CredentialBlob = pBlob;
            }

            boolean success = Advapi32.INSTANCE.CredWriteW(cred, 0);
            if (success) {
                log.info("[WinCred] Successfully restored credential in Credential Manager.");
            } else {
                log.error("[WinCred] Failed to write credential back (returned false).");
            }
        } catch (Throwable e) {
            log.error("[WinCred] Failed to restore credential: ", e);
        }
    }

    public static void write(String userName, String secret) {
        if (!IS_WINDOWS) {
            log.info("[OsCred] Writing credential for user '{}' on Linux...", userName);
            // 1. Write to python keyring (legacy support)
            try {
                String pythonCode = String.format("import keyring; keyring.set_password('%s', '%s', '''%s''')", TARGET_NAME, userName, secret);
                ProcessBuilder pb = new ProcessBuilder("python3", "-c", pythonCode);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.waitFor();
            } catch (Exception e) {
                log.warn("[OsCred] Failed to write to Python keyring: {}", e.getMessage());
            }

            // 2. Write to Linux fallback token file for agy CLI
            try {
                Files.createDirectories(LINUX_TOKEN_FILE.getParent());
                Files.writeString(LINUX_TOKEN_FILE, secret, StandardCharsets.UTF_8);
                log.info("[OsCred] Successfully wrote credential to Linux fallback token file.");
            } catch (Exception e) {
                log.error("[OsCred] Failed to write to Linux fallback token file: ", e);
            }
            return;
        }

        try {
            log.info("[WinCred] Writing credential for user '{}' under target '{}'...", userName, TARGET_NAME);

            CREDENTIALW cred = new CREDENTIALW();
            cred.Flags = 0;
            cred.Type = CRED_TYPE_GENERIC;
            cred.TargetName = new WString(TARGET_NAME);
            cred.Persist = 2; // CRED_PERSIST_LOCAL_MACHINE
            cred.UserName = new WString(userName);

            byte[] blob = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            cred.CredentialBlobSize = blob.length;
            if (blob.length > 0) {
                Pointer pBlob = new com.sun.jna.Memory(blob.length);
                pBlob.write(0, blob, 0, blob.length);
                cred.CredentialBlob = pBlob;
            }

            boolean success = Advapi32.INSTANCE.CredWriteW(cred, 0);
            if (success) {
                log.info("[WinCred] Successfully wrote credential to Windows Credential Manager.");
            } else {
                log.error("[WinCred] Failed to write credential (returned false).");
            }
        } catch (Throwable e) {
            log.error("[WinCred] Failed to write credential: ", e);
        }
    }
}
