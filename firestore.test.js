const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_bd_123";
const BOB_UID = "bob_bd_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function sampleProfile(uid, ts) {
  return {
    userId: uid,
    displayName: "Tasnim Rahman",
    email: "tasnim@example.com",
    phone: "+8801711223344",
    gender: "Female",
    age: 29,
    bloodGroup: "O+",
    division: "Dhaka",
    district: "Dhaka",
    area: "Dhanmondi",
    emergencyContactName: "Kamal Rahman",
    emergencyContactPhone: "+8801811223344",
    language: "en",
    use24HourTime: false,
    darkMode: "system",
    notifyConfirmations: true,
    notifyReminders: true,
    notifyAnnouncements: true,
    createdAt: ts,
    updatedAt: ts,
  };
}

function sampleAppointment(uid, ts, status = "confirmed") {
  return {
    userId: uid,
    doctorId: "doc_1",
    doctorName: "Prof. Dr. Mahbubur Rahman",
    specialization: "Cardiology",
    hospitalId: "hosp_1",
    hospitalName: "Square Hospitals Ltd.",
    chamberName: "Square Cardiac Center, Panthapath",
    appointmentDate: "2026-11-20",
    slotTime: "18:00",
    slotKey: "doc_1_Square Cardiac Center, Panthapath_2026-11-20_18:00",
    patientName: "Tasnim Rahman",
    patientPhone: "+8801711223344",
    consultationFeeBdt: 1500,
    notes: "Follow-up ECG review",
    status,
    createdAt: ts,
    updatedAt: ts,
  };
}

function sampleTestBooking(uid, ts, status = "confirmed") {
  return {
    userId: uid,
    testId: "test_cbc",
    testName: "Complete Blood Count (CBC)",
    centerId: "diag_1",
    centerName: "Popular Diagnostic Centre (Dhanmondi)",
    bookingDate: "2026-11-21",
    timeWindow: "08:00 - 10:00",
    homeCollection: true,
    collectionAddress: "House 12, Road 7, Dhanmondi, Dhaka",
    patientName: "Tasnim Rahman",
    patientPhone: "+8801711223344",
    priceBdt: 450,
    status,
    createdAt: ts,
    updatedAt: ts,
  };
}

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

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("appointments").where("userId", "==", ALICE_UID).get());
  await assertFails(unauthDb.collection("appointments").doc("appt_1").set(sampleAppointment(ALICE_UID, ts)));
  await assertFails(unauthDb.collection("reviews").where("targetType", "==", "hospital").get());
});

test("User profile: owner create, partial update, cross-user isolation, and ghost-field rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(sampleProfile(ALICE_UID, ts)));

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      area: "Banani",
      language: "bn",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field (Red Team shadow update)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject cross-user read or write
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ area: "Hijacked", updatedAt: ts }));
});

test("User subcollections (favorites, medicalRecords, notifications): owner-scoped queries and writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  // Empty subcollection list must succeed for owner, fail for non-owner
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("favorites").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("favorites").get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("hosp_1").set({
      targetId: "hosp_1",
      targetType: "hospital",
      title: "Square Hospitals Ltd.",
      subtitle: "Panthapath, Dhaka",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("medicalRecords").doc("rec_1").set({
      title: "CBC & Lipid Profile",
      recordType: "Lab Report",
      facilityName: "Popular Diagnostic Centre",
      recordDate: "2026-11-15",
      notes: "Normal range",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("notif_1").set({
      title: "Appointment Confirmed",
      body: "Confirmed for Nov 20 at 6:00 PM",
      category: "appointment",
      read: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("notif_1").update({
      read: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Appointments: create, filtered list, reschedule, terminal state locking, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("appointments").doc("appt_1").set(sampleAppointment(ALICE_UID, ts))
  );

  // Unfiltered query must fail; userId-filtered query must succeed
  await assertFails(aliceDb.collection("appointments").get());
  await assertSucceeds(aliceDb.collection("appointments").where("userId", "==", ALICE_UID).get());

  // Cross-user read/update must fail
  await assertFails(bobDb.collection("appointments").doc("appt_1").get());
  await assertFails(
    bobDb.collection("appointments").doc("appt_1").update({ status: "cancelled", updatedAt: ts })
  );

  // Reschedule succeeds while confirmed
  await assertSucceeds(
    aliceDb.collection("appointments").doc("appt_1").update({
      appointmentDate: "2026-11-22",
      slotTime: "19:00",
      slotKey: "doc_1_Square Cardiac Center, Panthapath_2026-11-22_19:00",
      notes: "Rescheduled evening slot",
      status: "confirmed",
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Cancel sets terminal state
  await assertSucceeds(
    aliceDb.collection("appointments").doc("appt_1").update({
      status: "cancelled",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Further updates after terminal state must fail
  await assertFails(
    aliceDb.collection("appointments").doc("appt_1").update({
      status: "confirmed",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Test bookings: create, filtered list, update, and terminal state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("testBookings").doc("tb_1").set(sampleTestBooking(ALICE_UID, ts))
  );
  await assertSucceeds(aliceDb.collection("testBookings").where("userId", "==", ALICE_UID).get());
  await assertFails(aliceDb.collection("testBookings").get());

  await assertSucceeds(
    aliceDb.collection("testBookings").doc("tb_1").update({
      bookingDate: "2026-11-23",
      timeWindow: "10:00 - 12:00",
      homeCollection: true,
      collectionAddress: "Road 11, Banani, Dhaka",
      status: "cancelled",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Locked after cancellation
  await assertFails(
    aliceDb.collection("testBookings").doc("tb_1").update({
      bookingDate: "2026-11-24",
      timeWindow: "08:00 - 10:00",
      homeCollection: false,
      collectionAddress: "",
      status: "confirmed",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Reviews and review reports: multi-criteria validation, community read, and author-only edit", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("reviews").doc("rev_1").set({
      userId: ALICE_UID,
      authorName: "Tasnim Rahman",
      targetId: "hosp_1",
      targetType: "hospital",
      overallRating: 5,
      doctorBehaviorRating: 5,
      staffBehaviorRating: 4,
      waitingTimeRating: 4,
      cleanlinessRating: 5,
      comment: "Very organized cardiac emergency reception and clean wards.",
      verifiedBooking: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Bob can read reviews filtered by targetType & targetId
  await assertSucceeds(
    bobDb
      .collection("reviews")
      .where("targetId", "==", "hosp_1")
      .where("targetType", "==", "hospital")
      .get()
  );

  // Bob cannot edit Alice's review
  await assertFails(
    bobDb.collection("reviews").doc("rev_1").update({
      comment: "Tampered",
      updatedAt: ts,
    })
  );

  // Bob can report Alice's existing review
  await assertSucceeds(
    bobDb.collection("reviewReports").doc("rep_1").set({
      userId: BOB_UID,
      reviewId: "rev_1",
      reason: "Spam or duplicate",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Report for non-existent review fails
  await assertFails(
    bobDb.collection("reviewReports").doc("rep_2").set({
      userId: BOB_UID,
      reviewId: "rev_missing",
      reason: "Fake",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});
