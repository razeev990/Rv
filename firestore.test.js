const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function createValidPayload(userId, mediaId = "movie_101", status = "WANT_TO_WATCH") {
  const now = new Date();
  return {
    userId: userId,
    mediaId: mediaId,
    title: "Inception",
    mediaType: "MOVIE",
    status: status,
    releaseStatus: "RELEASED",
    releaseYear: "2010",
    rating: 8.8,
    posterUrl: "https://image.tmdb.org/t/p/w500/poster.jpg",
    backdropUrl: "https://image.tmdb.org/t/p/w780/backdrop.jpg",
    overview: "A thief who steals corporate secrets through dream-sharing technology.",
    trailerUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
    fullMovieUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
    genres: "Sci-Fi, Action, Thriller",
    castSummary: "Leonardo DiCaprio|Dom Cobb;Joseph Gordon-Levitt|Arthur",
    userNotes: "Must watch in IMAX",
    userRating: 9,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot read or write watchlist", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("watchlist").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("watchlist")
      .doc("movie_101")
      .set(createValidPayload(ALICE_UID))
  );
});

test("Authenticated user: can create, read, update, and delete own watchlist item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("watchlist")
    .doc("movie_101");

  const payload = createValidPayload(ALICE_UID, "movie_101", "WANT_TO_WATCH");
  await assertSucceeds(docRef.set(payload));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("watchlist").get()
  );

  // Update status from WANT_TO_WATCH to WATCHED preserving createdAt
  await assertSucceeds(
    docRef.update({
      status: "WATCHED",
      userRating: 10,
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(docRef.delete());
});

test("Cross-user isolation: Alice cannot read or modify Bob's watchlist", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("watchlist")
      .doc("movie_101")
      .set(createValidPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDocRef = aliceDb
    .collection("users")
    .doc(BOB_UID)
    .collection("watchlist")
    .doc("movie_101");

  await assertFails(bobDocRef.get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("watchlist").get()
  );
  await assertFails(bobDocRef.delete());
});

test("Schema validation: rejects shadow fields and invalid status enum", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("watchlist")
    .doc("movie_101");

  const badStatusPayload = {
    ...createValidPayload(ALICE_UID),
    status: "INVALID_STATUS",
  };
  await assertFails(docRef.set(badStatusPayload));

  const ghostFieldPayload = {
    ...createValidPayload(ALICE_UID),
    isAdminHack: true,
  };
  await assertFails(docRef.set(ghostFieldPayload));
});
