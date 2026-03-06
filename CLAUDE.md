# MatCraft Minigames

## Deploy

When the user says "deploy", "deploy it", or similar:

1. Build: `./gradlew build`
2. Read `.env` for SSH credentials (SERVER_HOST, SERVER_PORT, SERVER_USERNAME, SERVER_PASSWORD)
3. Upload `build/libs/matminigames-1.0.0.jar` to `/home/debian/minecraft/survie/mods/matminigames-1.0.0.jar` via SFTP using paramiko
4. Restart: `sudo systemctl restart minecraft-survie`
5. Verify: `sudo systemctl is-active minecraft-survie`

Python SSH/SFTP pattern:
```python
import paramiko
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(host, port=port, username=user, password=password)
# SFTP upload
sftp = ssh.open_sftp()
sftp.put(local_path, remote_path)
sftp.close()
# Remote commands
stdin, stdout, stderr = ssh.exec_command(cmd)
out = stdout.read().decode('utf-8', errors='replace')
ssh.close()
```

Never hardcode credentials — always read from `.env`.
