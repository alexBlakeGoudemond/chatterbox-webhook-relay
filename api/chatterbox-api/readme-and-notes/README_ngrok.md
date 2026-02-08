# NGrok README

## Install

```bash
scoop install ngrok
```

Then make an account for the free tier. 

Navigate to `Getting Started > Auth token` to get your Auth Token, then run:

```bash
ngrok config add-authtoken 36KH8zIeaBvUrCQodLbsvDkbvPj_3GQzVRsQ4vC8R6Rx7SVjD
```

## use Ngrok

```bash
ngrok http 3002
```

(Custom domains only possible on paid tier)

## Bonus Features

Navigate to `http://localhost:4040`

We see:
- Request logs
- Response bodies
- Replays
- Timing breakdowns

Much better than localtunnel