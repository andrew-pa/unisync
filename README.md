# Unisync

An exploration of the architectural concepts from https://engineering.fb.com/2020/03/02/data-infrastructure/messenger/ for our CS501R "Software Architecture" class.

- `app/` contains a demo Android app and a Kotlin implementation of a portable client library (`app/unisync-client`)
- `terraform/` has Terraform files to deploy the backend for the demo app.
- `server/` contains our backend implementation which currently runs on AWS/Lambda. (The main sync algorithm is in `com.lightspeed.unisync.core.SyncAlgorithm` and is an implementation of ideas from https://unterwaditzer.net/2016/sync-algorithm.html)
