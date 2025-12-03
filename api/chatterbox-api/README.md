# README

There are several README documents created to explain what is going on here. It is worth glossing over them, as some
show how you can verify things are working and others explain how its working

To find these README files:

Powershell:
```bash
Get-ChildItem -Recurse -Filter "README*" | Select-Object FullName
```

Bash:
```bash
find . -type f -iname "readme*
```

# How to Confirm things Working?

## Quick Commands

LocalTunnel:

```bash
lt --port 3002 --subdomain chatterbox
```

NGrok:

```bash
ngrok http 3002
```

```bash
curl -X POST https://<publicFacingURL>/chatterbox/github -H "Content-Type: application/json" -H "X-GitHub-Event: push" -H "X-GitHub-Delivery: test123" -H "X-Hub-Signature-256: sha256=2677ad3e7c090b2fa2c0fb13020d66d5420879b8316eb356a2d60fb9073bc778" -d '{"hello":"world"}'
```