const toast = document.getElementById("toast");

function showToast(message, isError = false) {
  if (!toast) {
    return;
  }

  toast.textContent = message;
  toast.classList.toggle("is-error", isError);
  toast.classList.add("is-visible");
  toast.setAttribute("aria-hidden", "false");

  window.clearTimeout(showToast.timeoutId);
  showToast.timeoutId = window.setTimeout(() => {
    toast.classList.remove("is-visible");
    toast.setAttribute("aria-hidden", "true");
  }, 3600);
}

function formToJson(form) {
  return Object.fromEntries(new FormData(form).entries());
}

async function submitJson(url, form, successMessage) {
  const submitButton = form.querySelector('button[type="submit"]');
  const originalText = submitButton ? submitButton.textContent : "";

  if (submitButton) {
    submitButton.disabled = true;
    submitButton.textContent = "Working...";
  }

  try {
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(formToJson(form)),
    });
    const rawBody = await response.text();
    const payload = rawBody ? JSON.parse(rawBody) : {};

    if (!response.ok) {
      throw new Error(payload.message || "Request failed.");
    }

    showToast(payload.message || successMessage || "Saved successfully.");
    form.reset();
    return payload;
  } catch (error) {
    showToast(error.message, true);
    return null;
  } finally {
    if (submitButton) {
      submitButton.disabled = false;
      submitButton.textContent = originalText;
    }
  }
}

function setupMobileNavigation() {
  const button = document.querySelector(".menu-toggle");
  const nav = document.querySelector("[data-nav]");

  if (!button || !nav) {
    return;
  }

  button.addEventListener("click", () => {
    const isOpen = nav.classList.toggle("is-open");
    button.setAttribute("aria-expanded", String(isOpen));
  });

  nav.querySelectorAll("a").forEach((link) => {
    link.addEventListener("click", () => {
      nav.classList.remove("is-open");
      button.setAttribute("aria-expanded", "false");
    });
  });
}

function setupReveals() {
  const reveals = document.querySelectorAll(".reveal");

  if (!("IntersectionObserver" in window) || reveals.length === 0) {
    reveals.forEach((item) => item.classList.add("is-visible"));
    return;
  }

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("is-visible");
          observer.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.18 }
  );

  reveals.forEach((item) => observer.observe(item));
}

function setupTracking() {
  const form = document.getElementById("tracking-form");
  const result = document.getElementById("tracking-result");

  if (!form || !result) {
    return;
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const data = formToJson(form);
    const reference = String(data.reference || "").trim();

    if (!reference) {
      showToast("Enter a shipment reference to continue.", true);
      return;
    }

    result.innerHTML = '<p class="placeholder-copy">Loading shipment details...</p>';

    try {
      const response = await fetch(`/api/track?reference=${encodeURIComponent(reference)}`);
      const rawBody = await response.text();
      const payload = rawBody ? JSON.parse(rawBody) : {};

      if (!response.ok) {
        throw new Error(payload.message || "Unable to retrieve shipment.");
      }

      const { shipment } = payload;
      const issuesMarkup = Array.isArray(shipment.issues) && shipment.issues.length
        ? `
          <div class="issue-list">
            ${shipment.issues
              .map(
                (issue) => `
                  <article class="event-item issue-item issue-${String(issue.severity || "").toLowerCase()}">
                    <strong>${issue.severity} issue: ${issue.title}</strong>
                    <span>${issue.detail}</span>
                    <p><strong>Owner:</strong> ${issue.owner}</p>
                    <p><strong>Action:</strong> ${issue.action}</p>
                  </article>
                `
              )
              .join("")}
          </div>
        `
        : '<p class="placeholder-copy">No active issues on this shipment.</p>';
      const eventsMarkup = shipment.events
        .map(
          (eventItem) => `
            <article class="event-item">
              <strong>${eventItem.label}</strong>
              <span>${eventItem.location}</span>
              <p>${new Date(eventItem.timestamp).toLocaleString()}</p>
            </article>
          `
        )
        .join("");

      result.innerHTML = `
        <div class="tracker-meta">
          <p class="eyebrow">Current Shipment</p>
          <h3>${shipment.reference}</h3>
          <p>${shipment.status} at ${shipment.currentLocation}</p>
          <div class="progress-shell" aria-hidden="true">
            <span style="width: ${shipment.progress}%;"></span>
          </div>
        </div>
        <div class="tracker-grid">
          <article>
            <strong>Mode</strong>
            <span>${shipment.mode}</span>
          </article>
          <article>
            <strong>ETA</strong>
            <span>${shipment.eta}</span>
          </article>
          <article>
            <strong>Destination</strong>
            <span>${shipment.destination}</span>
          </article>
        </div>
        <div class="tracker-meta">
          <p class="eyebrow">Ops Summary</p>
          <p>${shipment.summary || "No summary available."}</p>
          <p><strong>Next action:</strong> ${shipment.nextAction || "Awaiting update."}</p>
          <p><strong>Support owner:</strong> ${shipment.supportOwner || "LAFL Operations"}</p>
        </div>
        ${issuesMarkup}
        <div class="event-list">${eventsMarkup}</div>
      `;
    } catch (error) {
      result.innerHTML = `<p class="placeholder-copy">${error.message}</p>`;
      showToast(error.message, true);
    }
  });
}

function setupForms() {
  const quoteForm = document.getElementById("quote-form");
  const contactForm = document.getElementById("contact-form");
  const signupForm = document.getElementById("signup-form");

  if (quoteForm) {
    quoteForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      await submitJson(
        "/api/quotes",
        quoteForm,
        "Quote request received."
      );
    });
  }

  if (contactForm) {
    contactForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      await submitJson(
        "/api/contact",
        contactForm,
        "Message sent."
      );
    });
  }

  if (signupForm) {
    signupForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const payload = await submitJson(
        "/api/signup",
        signupForm,
        "Portal account created."
      );

      if (payload && payload.redirectTo) {
        window.setTimeout(() => {
          window.location.href = payload.redirectTo;
        }, 700);
      }
    });
  }
}

setupMobileNavigation();
setupReveals();
setupTracking();
setupForms();
