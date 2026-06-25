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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class WindowsCredentialManager {

    private static final Logger log = LoggerFactory.getLogger(WindowsCredentialManager.class);

    private static final String TARGET_NAME = "gemini:antigravity";
    private static final int CRED_TYPE_GENERIC = 1;

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
        } catch (Exception e) {
            log.error("[WinCred] Failed to backup credential: ", e);
            return null;
        }
    }

    public static void restore(CredentialBackup backup) {
        if (backup == null) {
            try {
                log.info("[WinCred] No backup to restore. Ensuring target '{}' is cleared...", TARGET_NAME);
                Advapi32.INSTANCE.CredDeleteW(new WString(TARGET_NAME), CRED_TYPE_GENERIC, 0);
            } catch (Exception e) {
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
        } catch (Exception e) {
            log.error("[WinCred] Failed to restore credential: ", e);
        }
    }

    public static void write(String userName, String secret) {
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
        } catch (Exception e) {
            log.error("[WinCred] Failed to write credential: ", e);
        }
    }
}
