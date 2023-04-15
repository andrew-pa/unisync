# Unisync

An exploration of the architectural concepts from https://engineering.fb.com/2020/03/02/data-infrastructure/messenger/ for our CS501R "Software Architecture" class.

Demo of syncing across two devices staying in sync with each other via the BE system. New data is regularly pulled by the devices. The swipe to refresh is just a convineint way to refresh the UI. It does not make a network call at that point as the data has already been transfered to the on device database:

https://user-images.githubusercontent.com/14169748/232162807-33e68149-e2eb-4cd8-8490-efa6c484f910.mov



- `app/` contains a demo Android app and a Kotlin implementation of a portable client library (`app/unisync-client`)
- `terraform/` has Terraform files to deploy the backend for the demo app.
- `server/` contains our backend implementation which currently runs on AWS/Lambda. (The main sync algorithm is in `com.lightspeed.unisync.core.SyncAlgorithm` and is an implementation of ideas from https://unterwaditzer.net/2016/sync-algorithm.html)
