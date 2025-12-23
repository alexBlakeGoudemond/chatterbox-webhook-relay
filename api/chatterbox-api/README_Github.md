# README Github

In order to make requests towards Github's API, we need an auth token


### How to Get a GitHub Token

1. Go to [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens)
2. Click "Generate new token (classic)"
3. Give your token a descriptive name (e.g., "Repository Management Portal")
4. Set expiration as needed
5. Select the following scopes:
    - `repo` (Full control of private repositories)
    - `read:org` (Read org and team membership)
    - `write:org` (Read and write org and team membership)
6. Click "Generate token"
7. **Important**: Copy the token immediately as you won't be able to see it again

> By doing this, you will be able to make requests for repositories in Organisations you are part of
> as well as repositories that are owned by you
