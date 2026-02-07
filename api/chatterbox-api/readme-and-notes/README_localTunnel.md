# LocalTunnel README

## Create and test with LocalTunnel

LocalTunnel is creating a public facing URL with SSH for us to use

Start by following the steps outline in Docker-Compose

In a dedicated terminal run:

```bash
lt --port 3002 --subdomain chatterbox
```

(this will stay open)

In a separate terminal:

```bash
curl -X POST https://chatterbox.loca.lt/chatterbox/github -H "Content-Type: application/json" -H "X-GitHub-Event: push" -H "X-GitHub-Delivery: 123" -H "X-Hub-Signature-256: sha256=2677ad3e7c090b2fa2c0fb13020d66d5420879b8316eb356a2d60fb9073bc778" -d '{"hello":"world"}'
```

> (notice the url) A response should come through, with something like "Missing Signature"

## LocalTunnel on Port 443

We have set up Port 443 to receive HTTPS requests. LocalTunnel is a tunneling tool.

It is essentially a wrapper that takes in HTTPS requests and passes them along to localhost through HTTP

The way LocalTunnel works is like this:
1. HTTPS urls come in, LocalTunnel receives it on the configured (temporary) domain
2. LocalTunnel terminates the HTTPS connection, having the request in plain HTTP
3. LocalTunnel forwards the plain HTTP request to localhost: it relies on the fact that localhost is trusted

This means that if LocalTunnel is set to redirect traffic to PORT 443,
then port 443 will receive an HTTP Request.
Port 443 is set to accept only SSL requests (HTTPS) so requests made
will yield an HTTP Status of 400 with this meesage:
`400 The plain HTTP request was sent to HTTPS port`

## Running in a browser

LocalStack may ask for a password.
The tunnel password is the public IP of the computer running the localtunnel client (or the vpn's public IP if connected to one)
To find the password, consider: `https://loca.lt/mytunnelpassword`

Note that if your url is resolved to Spring, it defaults to a GET request
