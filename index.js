const express = require("express");
const crypto = require("crypto");
const fsSync = require("fs");
const fs = require("fs/promises");
const path = require("path");

let nodemailer = null;
let Firestore = null;

try {
  nodemailer = require("nodemailer");
} catch (error) {
  nodemailer = null;
}

try {
  ({ Firestore } = require("@google-cloud/firestore"));
} catch (error) {
  Firestore = null;
}

const app = express();
const PORT = process.env.PORT || 3000;
const publicDirectory = path.join(__dirname, "public");
const angularDistDirectory = path.join(
  __dirname,
  "frontend",
  "dist",
  "frontend",
  "browser"
);
const hasAngularBuild = fsSync.existsSync(
  path.join(angularDistDirectory, "index.html")
);
const staticDirectory = hasAngularBuild ? angularDistDirectory : publicDirectory;
const dataDirectory = path.join(__dirname, "data");
const appBaseUrl = process.env.APP_BASE_URL || `http://localhost:${PORT}`;
const gcpProjectId =
  process.env.GOOGLE_CLOUD_PROJECT ||
  process.env.GCLOUD_PROJECT ||
  process.env.PROJECT_ID ||
  "";
const useFirestore =
  String(process.env.USE_FIRESTORE || "").toLowerCase() === "true" ||
  Boolean(gcpProjectId);

const collections = {
  contacts: "contacts.json",
  quotes: "quotes.json",
  shipments: "shipments.json",
  users: "users.json",
};

const defaultData = {
  contacts: [],
  quotes: [],
  users: [],
  shipments: [
    {
      reference: "LAFL-24017",
      client: "Northline Retail",
      mode: "Ocean + Road",
      status: "On Schedule",
      progress: 72,
      eta: "2026-03-29",
      currentLocation: "Rotterdam Distribution Hub",
      destination: "Hamburg, Germany",
      lastUpdated: "2026-03-26T09:20:00.000Z",
      events: [
        {
          label: "Container cleared customs",
          location: "Rotterdam Port",
          timestamp: "2026-03-25T13:15:00.000Z",
        },
        {
          label: "Truck transfer assigned",
          location: "Rotterdam Distribution Hub",
          timestamp: "2026-03-26T09:20:00.000Z",
        },
      ],
      issues: [],
      summary: "Cargo is moving on plan with no open operational exceptions.",
      nextAction: "Final truck dispatch to Hamburg on arrival slot confirmation.",
      supportOwner: "Ava Patel",
    },
    {
      reference: "LAFL-98241",
      client: "AeroPharm Labs",
      mode: "Air Freight",
      status: "Customs Review",
      progress: 54,
      eta: "2026-03-30",
      currentLocation: "JFK International Cargo Center",
      destination: "Toronto, Canada",
      lastUpdated: "2026-03-26T06:40:00.000Z",
      events: [
        {
          label: "Flight landed",
          location: "JFK International Cargo Center",
          timestamp: "2026-03-26T02:05:00.000Z",
        },
        {
          label: "Documentation submitted",
          location: "JFK International Cargo Center",
          timestamp: "2026-03-26T06:40:00.000Z",
        },
      ],
      issues: [
        {
          severity: "High",
          title: "Customs hold on active ingredient certificate",
          detail:
            "Border review flagged a mismatch between the uploaded certificate and the carton batch number.",
          owner: "JFK Customs Desk",
          action: "Upload corrected certificate before 3:00 PM ET to avoid a 24-hour delay.",
        },
        {
          severity: "Medium",
          title: "Temperature logger check required",
          detail:
            "Cold-chain audit requested one manual validation before final release from the cargo center.",
          owner: "Station Handling Team",
          action: "Confirm logger reading and re-seal container after inspection.",
        },
      ],
      summary:
        "Shipment arrived, but release is blocked until customs documentation and cold-chain validation are cleared.",
      nextAction: "Use this sample ID to review issue handling in the tracker UI and ops endpoint.",
      supportOwner: "Jordan Lee",
    },
    {
      reference: "LAFL-77802",
      client: "Everstone Interiors",
      mode: "Project Cargo",
      status: "Booked",
      progress: 18,
      eta: "2026-04-04",
      currentLocation: "Chennai Consolidation Center",
      destination: "Dubai, UAE",
      lastUpdated: "2026-03-25T17:10:00.000Z",
      events: [
        {
          label: "Cargo inspection complete",
          location: "Chennai Consolidation Center",
          timestamp: "2026-03-25T11:30:00.000Z",
        },
        {
          label: "Export booking confirmed",
          location: "Chennai Consolidation Center",
          timestamp: "2026-03-25T17:10:00.000Z",
        },
      ],
      issues: [
        {
          severity: "Low",
          title: "Crating dimensions pending final approval",
          detail:
            "Final crate measurements are awaiting shipper sign-off before pickup scheduling can be locked.",
          owner: "Project Cargo Planning",
          action: "Approve crate plan to release pickup slot confirmation.",
        },
      ],
      summary:
        "Project cargo booking is confirmed and waiting on final crate approval before dispatch scheduling.",
      nextAction: "Collect final packaging approval from the origin team.",
      supportOwner: "Mina Joseph",
    },
  ],
};

app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(express.static(staticDirectory));
app.use("/images", express.static(path.join(publicDirectory, "images")));

function pageResponse(fallbackFileName) {
  return (req, res) => {
    if (hasAngularBuild) {
      return res.sendFile(path.join(angularDistDirectory, "index.html"));
    }

    return res.sendFile(path.join(publicDirectory, fallbackFileName));
  };
}

function normalizeText(value) {
  return String(value || "").trim().replace(/\s+/g, " ");
}

function normalizeEmail(value) {
  return normalizeText(value).toLowerCase();
}

function isEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function createId(prefix) {
  return `${prefix}-${crypto.randomUUID().split("-")[0]}`;
}

function keysMatch(expected, provided) {
  if (!expected || !provided) {
    return false;
  }

  const expectedBuffer = Buffer.from(expected);
  const providedBuffer = Buffer.from(provided);

  if (expectedBuffer.length !== providedBuffer.length) {
    return false;
  }

  return crypto.timingSafeEqual(expectedBuffer, providedBuffer);
}

function hashPassword(password) {
  const salt = crypto.randomBytes(16).toString("hex");
  const hash = crypto.scryptSync(password, salt, 64).toString("hex");
  return { salt, hash };
}

function formatDateTime(value) {
  return new Date(value).toLocaleString("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function getMailConfig() {
  return {
    host: process.env.SMTP_HOST || "smtp.gmail.com",
    port: Number(process.env.SMTP_PORT || 465),
    secure: String(process.env.SMTP_SECURE || "true") === "true",
    user: process.env.SMTP_USER || process.env.GMAIL_USER || "",
    pass: process.env.SMTP_PASS || process.env.GMAIL_APP_PASSWORD || "",
    to:
      process.env.MAIL_TO ||
      process.env.GMAIL_TO ||
      process.env.SMTP_USER ||
      process.env.GMAIL_USER ||
      "",
    from:
      process.env.MAIL_FROM ||
      process.env.SMTP_USER ||
      process.env.GMAIL_USER ||
      "",
  };
}

function isMailConfigured() {
  const config = getMailConfig();
  return Boolean(
    nodemailer && config.user && config.pass && config.to && config.from
  );
}

let transporter;
let firestore;

function isFirestoreConfigured() {
  return Boolean(Firestore && gcpProjectId);
}

function getFirestore() {
  if (!useFirestore || !isFirestoreConfigured()) {
    return null;
  }

  if (!firestore) {
    firestore = new Firestore({ projectId: gcpProjectId });
  }

  return firestore;
}

function getTransporter() {
  if (!isMailConfigured()) {
    return null;
  }

  if (!transporter) {
    const config = getMailConfig();
    transporter = nodemailer.createTransport({
      host: config.host,
      port: config.port,
      secure: config.secure,
      auth: {
        user: config.user,
        pass: config.pass,
      },
    });
  }

  return transporter;
}

function renderEmailTemplate({ title, intro, fields, footerNote }) {
  const rows = fields
    .map(
      ({ label, value }) => `
        <tr>
          <td style="padding:12px 16px;border-bottom:1px solid #e5e7eb;color:#6b7280;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;">${escapeHtml(label)}</td>
          <td style="padding:12px 16px;border-bottom:1px solid #e5e7eb;color:#111827;font-size:14px;line-height:1.6;">${escapeHtml(value)}</td>
        </tr>
      `
    )
    .join("");

  return `
    <div style="background:#f3f4f6;padding:24px;font-family:Arial,sans-serif;">
      <table role="presentation" style="width:100%;max-width:720px;margin:0 auto;background:#ffffff;border-radius:20px;overflow:hidden;border:1px solid #e5e7eb;">
        <tr>
          <td style="padding:24px 28px;background:#0b1722;color:#f9fafb;">
            <p style="margin:0 0 8px;font-size:12px;letter-spacing:0.16em;text-transform:uppercase;color:#f7c66d;">LAFL Logistics Portal</p>
            <h1 style="margin:0;font-size:28px;line-height:1.1;">${escapeHtml(title)}</h1>
            <p style="margin:12px 0 0;color:#cbd5e1;font-size:15px;line-height:1.6;">${escapeHtml(intro)}</p>
          </td>
        </tr>
        <tr>
          <td style="padding:0 0 8px;">
            <table role="presentation" style="width:100%;border-collapse:collapse;">${rows}</table>
          </td>
        </tr>
        <tr>
          <td style="padding:20px 28px;background:#f9fafb;color:#6b7280;font-size:13px;line-height:1.6;">
            ${escapeHtml(footerNote).replace(/\n/g, "<br>")}
          </td>
        </tr>
      </table>
    </div>
  `;
}

function renderTextEmail({ title, intro, fields, footerNote }) {
  const fieldLines = fields.map(({ label, value }) => `${label}: ${value}`).join("\n");
  return `${title}\n\n${intro}\n\n${fieldLines}\n\n${footerNote}`;
}

async function sendNotificationEmail({
  subject,
  title,
  intro,
  fields,
  replyTo,
}) {
  const mailConfig = getMailConfig();
  const activeTransporter = getTransporter();

  if (!activeTransporter) {
    return {
      status: "skipped",
      reason: "mail_not_configured",
    };
  }

  const footerNote = `Portal URL: ${appBaseUrl}\nGenerated at ${formatDateTime(
    new Date().toISOString()
  )}`;

  try {
    await activeTransporter.sendMail({
      from: mailConfig.from,
      to: mailConfig.to,
      replyTo,
      subject,
      text: renderTextEmail({ title, intro, fields, footerNote }),
      html: renderEmailTemplate({ title, intro, fields, footerNote }),
    });

    return {
      status: "sent",
      deliveredTo: mailConfig.to,
      sentAt: new Date().toISOString(),
    };
  } catch (error) {
    return {
      status: "failed",
      reason: error.message,
    };
  }
}

function getOpsKey(req) {
  return req.get("x-ops-key") || req.query.key || "";
}

function requireOpsAccess(req, res, next) {
  const expectedKey = process.env.OPS_API_KEY;

  if (!expectedKey) {
    return res.status(503).json({
      message: "OPS_API_KEY is not configured on this service.",
    });
  }

  if (!keysMatch(expectedKey, getOpsKey(req))) {
    return res.status(401).json({
      message: "Unauthorized ops access.",
    });
  }

  return next();
}

async function ensureDataFiles() {
  if (useFirestore && isFirestoreConfigured()) {
    return;
  }

  await fs.mkdir(dataDirectory, { recursive: true });

  await Promise.all(
    Object.entries(collections).map(async ([collection, fileName]) => {
      const filePath = path.join(dataDirectory, fileName);

      try {
        await fs.access(filePath);
      } catch (error) {
        await fs.writeFile(
          filePath,
          JSON.stringify(defaultData[collection], null, 2),
          "utf8"
        );
      }
    })
  );
}

async function readCollection(collection) {
  const db = getFirestore();

  if (db) {
    const snapshot = await db.collection(collection).orderBy("createdAt", "desc").get();

    if (snapshot.empty && Array.isArray(defaultData[collection]) && defaultData[collection].length > 0) {
      await seedFirestoreCollection(collection, defaultData[collection]);
      return defaultData[collection];
    }

    return snapshot.docs.map((document) => document.data());
  }

  const filePath = path.join(dataDirectory, collections[collection]);
  const fileContents = await fs.readFile(filePath, "utf8");
  return JSON.parse(fileContents);
}

async function writeCollection(collection, records) {
  const db = getFirestore();

  if (db) {
    const batch = db.batch();
    const collectionRef = db.collection(collection);
    const existingSnapshot = await collectionRef.get();

    existingSnapshot.forEach((document) => {
      batch.delete(document.ref);
    });

    records.forEach((record, index) => {
      const documentId = record.id || record.reference || `${collection}-${index + 1}`;
      batch.set(collectionRef.doc(documentId), record);
    });

    await batch.commit();
    return;
  }

  const filePath = path.join(dataDirectory, collections[collection]);
  await fs.writeFile(filePath, JSON.stringify(records, null, 2), "utf8");
}

async function appendRecord(collection, record) {
  const db = getFirestore();

  if (db) {
    const documentId = record.id || record.reference || createId(collection);
    const storedRecord = {
      ...record,
      id: record.id || documentId,
    };

    await db.collection(collection).doc(documentId).set(storedRecord);
    return storedRecord;
  }

  const records = await readCollection(collection);
  records.unshift(record);
  await writeCollection(collection, records);
  return record;
}

async function seedFirestoreCollection(collection, records) {
  const db = getFirestore();

  if (!db || !Array.isArray(records) || records.length === 0) {
    return;
  }

  const batch = db.batch();
  const collectionRef = db.collection(collection);

  records.forEach((record, index) => {
    const documentId = record.id || record.reference || `${collection}-${index + 1}`;
    batch.set(collectionRef.doc(documentId), record);
  });

  await batch.commit();
}

app.get("/", pageResponse("index.html"));
app.get("/signup", pageResponse("signup.html"));
app.get("/signup-success", pageResponse("signup_success.html"));
app.get("/login_page", (req, res) => res.redirect("/signup"));
app.get("/signin.html", (req, res) => res.redirect("/signup"));
app.get("/signup_success.html", (req, res) => res.redirect("/signup-success"));
app.get("/api/health", (req, res) => {
  res.json({
    ok: true,
    service: "lafl-logistics-portal",
    mailConfigured: isMailConfigured(),
    opsProtected: Boolean(process.env.OPS_API_KEY),
    sampleIssueReference: "LAFL-98241",
    storageMode: getFirestore() ? "firestore" : "local-json",
  });
});

app.get("/api/track", async (req, res, next) => {
  try {
    const reference = normalizeText(req.query.reference).toUpperCase();

    if (!reference) {
      return res.status(400).json({ message: "Shipment reference is required." });
    }

    const shipments = await readCollection("shipments");
    const shipment = shipments.find((item) => item.reference === reference);

    if (!shipment) {
      return res.status(404).json({
        message:
          "We could not find that shipment reference. Try LAFL-24017 or LAFL-98241.",
      });
    }

    return res.json({
      sampleIssueReference: "LAFL-98241",
      shipment,
    });
  } catch (error) {
    return next(error);
  }
});

app.get("/api/ops/overview", requireOpsAccess, async (req, res, next) => {
  try {
    const [contacts, quotes, users, shipments] = await Promise.all([
      readCollection("contacts"),
      readCollection("quotes"),
      readCollection("users"),
      readCollection("shipments"),
    ]);

    const recentActivity = [
      ...contacts.map((item) => ({
        type: "contact",
        id: item.id,
        title: `${item.name} sent a contact request`,
        createdAt: item.createdAt,
      })),
      ...quotes.map((item) => ({
        type: "quote",
        id: item.id,
        title: `${item.company} requested a quote`,
        createdAt: item.createdAt,
      })),
      ...users.map((item) => ({
        type: "signup",
        id: item.id,
        title: `${item.name} created portal access`,
        createdAt: item.createdAt,
      })),
    ]
      .sort((left, right) => new Date(right.createdAt) - new Date(left.createdAt))
      .slice(0, 10);

    return res.json({
      sampleIssueReference: "LAFL-98241",
      mailConfigured: isMailConfigured(),
      counts: {
        contacts: contacts.length,
        quotes: quotes.length,
        users: users.length,
        trackedShipments: shipments.length,
      },
      activeIssues: shipments
        .filter((shipment) => Array.isArray(shipment.issues) && shipment.issues.length > 0)
        .map((shipment) => ({
          reference: shipment.reference,
          status: shipment.status,
          issueCount: shipment.issues.length,
          summary: shipment.summary,
        })),
      recentActivity,
    });
  } catch (error) {
    return next(error);
  }
});

app.get("/api/ops/issues", requireOpsAccess, async (req, res, next) => {
  try {
    const shipments = await readCollection("shipments");

    return res.json({
      sampleIssueReference: "LAFL-98241",
      shipments: shipments.filter(
        (shipment) => Array.isArray(shipment.issues) && shipment.issues.length > 0
      ),
    });
  } catch (error) {
    return next(error);
  }
});

app.post("/api/contact", async (req, res, next) => {
  try {
    const name = normalizeText(req.body.name);
    const email = normalizeEmail(req.body.email);
    const company = normalizeText(req.body.company);
    const message = normalizeText(req.body.message);

    if (!name || !email || !message) {
      return res
        .status(400)
        .json({ message: "Name, email, and message are required." });
    }

    if (!isEmail(email)) {
      return res.status(400).json({ message: "Enter a valid email address." });
    }

    const record = {
      id: createId("msg"),
      name,
      email,
      company,
      message,
      createdAt: new Date().toISOString(),
    };

    record.notification = await sendNotificationEmail({
      subject: `LAFL Contact Request | ${name}`,
      title: "New contact request received",
      intro: "A visitor submitted the contact form on the logistics portal.",
      replyTo: email,
      fields: [
        { label: "Name", value: name },
        { label: "Email", value: email },
        { label: "Company", value: company || "Not provided" },
        { label: "Message", value: message },
        { label: "Created At", value: formatDateTime(record.createdAt) },
      ],
    });

    await appendRecord("contacts", record);

    return res.status(201).json({
      message: "Thanks. Our operations team will get back to you shortly.",
      record,
    });
  } catch (error) {
    return next(error);
  }
});

app.post("/api/quotes", async (req, res, next) => {
  try {
    const company = normalizeText(req.body.company);
    const contactName = normalizeText(req.body.contactName);
    const email = normalizeEmail(req.body.email);
    const origin = normalizeText(req.body.origin);
    const destination = normalizeText(req.body.destination);
    const serviceType = normalizeText(req.body.serviceType);
    const shipmentType = normalizeText(req.body.shipmentType);
    const cargoDetails = normalizeText(req.body.cargoDetails);

    if (
      !company ||
      !contactName ||
      !email ||
      !origin ||
      !destination ||
      !serviceType
    ) {
      return res.status(400).json({
        message:
          "Company, contact name, email, origin, destination, and service type are required.",
      });
    }

    if (!isEmail(email)) {
      return res.status(400).json({ message: "Enter a valid email address." });
    }

    const record = {
      id: createId("quote"),
      company,
      contactName,
      email,
      origin,
      destination,
      serviceType,
      shipmentType,
      cargoDetails,
      status: "Pending Review",
      createdAt: new Date().toISOString(),
    };

    record.notification = await sendNotificationEmail({
      subject: `LAFL Quote Request | ${company}`,
      title: "New quote request received",
      intro: "A client submitted a logistics quote request through the website.",
      replyTo: email,
      fields: [
        { label: "Company", value: company },
        { label: "Contact Name", value: contactName },
        { label: "Email", value: email },
        { label: "Service Type", value: serviceType },
        { label: "Origin", value: origin },
        { label: "Destination", value: destination },
        { label: "Shipment Type", value: shipmentType || "Not provided" },
        { label: "Cargo Details", value: cargoDetails || "Not provided" },
        { label: "Created At", value: formatDateTime(record.createdAt) },
      ],
    });

    await appendRecord("quotes", record);

    return res.status(201).json({
      message:
        "Quote request received. We will send a tailored logistics plan within one business day.",
      record,
    });
  } catch (error) {
    return next(error);
  }
});

app.post("/api/signup", async (req, res, next) => {
  try {
    const name = normalizeText(req.body.name);
    const email = normalizeEmail(req.body.email);
    const company = normalizeText(req.body.company);
    const phone = normalizeText(req.body.phone);
    const password = String(req.body.password || "").trim();
    const interest = normalizeText(req.body.interest);

    if (!name || !email || !company || !password) {
      return res
        .status(400)
        .json({ message: "Name, company, email, and password are required." });
    }

    if (!isEmail(email)) {
      return res.status(400).json({ message: "Enter a valid email address." });
    }

    if (password.length < 8) {
      return res.status(400).json({
        message: "Choose a password with at least 8 characters.",
      });
    }

    const users = await readCollection("users");
    const existingUser = users.find((user) => user.email === email);

    if (existingUser) {
      return res
        .status(409)
        .json({ message: "An account with that email already exists." });
    }

    const passwordDigest = hashPassword(password);
    const record = {
      id: createId("user"),
      name,
      email,
      company,
      phone,
      interest,
      passwordHash: passwordDigest.hash,
      passwordSalt: passwordDigest.salt,
      createdAt: new Date().toISOString(),
    };

    record.notification = await sendNotificationEmail({
      subject: `LAFL Portal Signup | ${company}`,
      title: "New portal signup created",
      intro:
        "A new client portal account was created. The password is intentionally excluded from email notifications.",
      replyTo: email,
      fields: [
        { label: "Name", value: name },
        { label: "Email", value: email },
        { label: "Company", value: company },
        { label: "Phone", value: phone || "Not provided" },
        { label: "Interest", value: interest || "Not provided" },
        { label: "Created At", value: formatDateTime(record.createdAt) },
      ],
    });

    users.unshift(record);
    await writeCollection("users", users);

    return res.status(201).json({
      message: "Portal access created. Your onboarding checklist is ready.",
      redirectTo: "/signup-success",
    });
  } catch (error) {
    return next(error);
  }
});

if (hasAngularBuild) {
  app.get("*", (req, res, next) => {
    if (req.path.startsWith("/api/")) {
      return next();
    }

    return res.sendFile(path.join(angularDistDirectory, "index.html"));
  });
}

app.use((error, req, res, next) => {
  console.error(error);

  if (req.path.startsWith("/api/")) {
    return res.status(500).json({
      message: "Something went wrong while processing the request.",
    });
  }

  return res.status(500).send("Something went wrong while loading the page.");
});

ensureDataFiles()
  .then(() => {
    app.listen(PORT, () => {
      console.log(`http://localhost:${PORT}/`);
    });
  })
  .catch((error) => {
    console.error("Unable to prepare local data files.", error);
    process.exit(1);
  });
