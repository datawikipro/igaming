# Working with Remote Docker (Windows Guide)

This guide explains how to configure your local machine to use a remote Docker engine (e.g., on a Linux server or a Kubernetes node).

## 1. Using Docker Contexts (Recommended)
Docker contexts allow you to easily switch between different Docker engines.

### Create a new context via SSH:
```powershell
docker context create remote-srv --docker "host=ssh://user@remote-ip"
```

### Switch to the remote context:
```powershell
docker context use remote-srv
```

### Switch back to local:
```powershell
docker context use default
# or
docker context use desktop-linux
```

---

## 2. Using Environment Variables
If you don't want to use contexts, you can set the `DOCKER_HOST` variable temporarily.

### PowerShell:
```powershell
$env:DOCKER_HOST = "ssh://user@remote-ip"
docker ps  # Now runs against the remote server
```

### To unset:
```powershell
$env:DOCKER_HOST = $null
```

---

## 3. SSH Configuration (Prerequisite)
For the `ssh://` protocol to work without passwords:
1. Generate a key: `ssh-keygen -t ed25519`
2. Copy it to the server: `ssh-copy-id user@remote-ip` (on Windows, you might need to manually add it to `~/.ssh/authorized_keys`).
3. Ensure the user is in the `docker` group on the server.

---

## 4. Troubleshooting
- **Connection refused**: Ensure the Docker daemon on the server is listening or that SSH access is allowed.
- **Permission denied**: Ensure your SSH user has rights to `/var/run/docker.sock` on the remote machine.
- **npipe errors**: Usually means you are trying to connect to local Docker Desktop while it is stopped. Switch contexts to fix this.
