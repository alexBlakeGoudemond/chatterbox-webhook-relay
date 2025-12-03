# NginX README

NginX is a reverse proxy tool that we are using to expose some parts of the API

## Self Signed Certificate

Self-signed certs allow local environment to look like production.
The browser/Postman/Spring Boot/Nginx config all behave as if it’s real TLS

We can then test:

- SSL termination
- Redirects (HTTP→HTTPS)
- Ciphers, HSTS, etc.
- Cert/key loading
- Proxy settings

### Installing Mkcert on Windows

`scoop bucket add extras`

`scoop install mkcert`

`mkcert -install` (install the local Certificate Authority in your system's trust store, allowing your browser
and OS to trust the certificates generated)

### Generating Cert

generate the Certificate
`mkcert -key-file key.pem -cert-file cert.pem localhost 127.0.0.1 ::1`

Once generated, 2 files will be created: `cert.pem` and `key.pem` - we must add those to Nginx and adjust the config to
reflect

## Testing Cert

> If testing and you see `Missing Signature` - just add the `X-Hub-Signature-256`

this should work (https - lt using port 3002 still):

```bash
curl -X POST https://chatterbox.loca.lt/chatterbox/github      -H "Content-Type: application/json"      -H "X-Hub-Signature-256: sha256=2677ad3e7c090b2fa2c0fb13020d66d5420879b8316eb356a2d60fb9073bc778"      -d '{"hello":"world"}'
```