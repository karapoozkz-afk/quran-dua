# Shared repentance wall: Firebase setup

The app talks to Firestore through its REST API; no Firebase SDK is bundled. Until the two
values below reach the build, the "Shared" tab says the wall opens soon.

1. Create a project at https://console.firebase.google.com (the free Spark plan is enough).
2. Build → Authentication → Sign-in method → enable **Anonymous**.
3. Build → Firestore Database → create a database (production mode; pick a region).
4. Firestore → Rules: paste `firestore.rules` and publish.
   Firestore → Indexes → Composite: add `posts`: `status` ↑, `lang` ↑, `createdAt` ↓
   (or run `firebase deploy --only firestore` with this folder).
5. Project settings → General: copy the **Project ID** and the **Web API key**.
6. GitHub → the repository → Settings → Secrets and variables → Actions: add
   `WALL_PROJECT_ID` and `WALL_API_KEY`. The next build turns the wall on.
   Locally: `./gradlew assembleDebug -PwallProjectId=… -PwallApiKey=…`.

## Moderation

New posts arrive in Firestore → `posts` with `status = "pending"`. Change it to `approved` to
publish, or `rejected` to hide it for good. Reports land in `reports` (`post` = the post id).
Deleting a post in the console removes it from the wall.
