/* =========================================
   GymBook — app.js  (v2)
   - localStorage persistence
   - event delegation (no inline onclick)
   - client-side validation
   - loading states on buttons
   - masked emails for non-admins
   - clean details panel (no ID, no booking list)
   - simplified parseJwtRole
   - auto-fill username in book modal
   ========================================= */

// =========================
// STATE
// =========================
let currentUser = null; // { username, role, token }

// Tracks which classId bookings are currently shown (for refresh after delete)
let activeBookingsClassId = null;

// =========================
// DOM REFS
// =========================
const classesContainer           = document.getElementById("classesContainer");
const bookingsContainer          = document.getElementById("bookingsContainer");
const classDetailsContainer      = document.getElementById("classDetailsContainer");

const openAuthBtn                = document.getElementById("openAuthBtn");
const closeAuthBtn               = document.getElementById("closeAuthBtn");
const authModal                  = document.getElementById("authModal");
const navUser                    = document.getElementById("navUser");
const navAvatar                  = document.getElementById("navAvatar");
const navRoleBadge               = document.getElementById("navRoleBadge");
const logoutButton               = document.getElementById("logoutButton");
const loginForm                  = document.getElementById("loginForm");
const loginUsername              = document.getElementById("loginUsername");
const loginPassword              = document.getElementById("loginPassword");
const registerForm               = document.getElementById("registerForm");
const registerUsername           = document.getElementById("registerUsername");
const registerPassword           = document.getElementById("registerPassword");

const searchForm                 = document.getElementById("searchForm");
const searchInstructor           = document.getElementById("searchInstructor");
const loadAllClassesButton       = document.getElementById("loadAllClassesButton");
const loadAvailableClassesButton = document.getElementById("loadAvailableClassesButton");

const adminModal                 = document.getElementById("adminModal");
const closeAdminBtn              = document.getElementById("closeAdminBtn");
const adminModalTitle            = document.getElementById("adminModalTitle");
const adminClassForm             = document.getElementById("adminClassForm");
const adminClassId               = document.getElementById("adminClassId");
const adminName                  = document.getElementById("adminName");
const adminInstructor            = document.getElementById("adminInstructor");
const adminDescription           = document.getElementById("adminDescription");
const adminDayOfWeek             = document.getElementById("adminDayOfWeek");
const adminStartTime             = document.getElementById("adminStartTime");
const adminDurationMinutes       = document.getElementById("adminDurationMinutes");
const adminMaxParticipants       = document.getElementById("adminMaxParticipants");
const adminResetButton           = document.getElementById("adminResetButton");

const bookModal                  = document.getElementById("bookModal");
const closeBookBtn               = document.getElementById("closeBookBtn");
const bookModalTitle             = document.getElementById("bookModalTitle");
const bookModalMeta              = document.getElementById("bookModalMeta");
const bookClassIdInput           = document.getElementById("bookClassId");
const bookNameInput              = document.getElementById("bookName");
const bookEmailInput             = document.getElementById("bookEmail");
const confirmBookBtn             = document.getElementById("confirmBookBtn");
const classIdSearchBlock         = document.getElementById("classIdSearchBlock");
const classIdSearchForm          = document.getElementById("classIdSearchForm");
const classIdSearchInput         = document.getElementById("classIdSearchInput");

// =========================
// TOAST
// =========================
let toastTimer;
function showToast(message, type = "info") {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.className = `toast show${type !== "info" ? " " + type : ""}`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove("show"), 3200);
}

// =========================
// AUTH HELPERS
// =========================
function getToken()   { return currentUser?.token || null; }
function isAdmin()    { return currentUser?.role === "ADMIN"; }
function isLoggedIn() { return !!currentUser; }

// Simplified: backend always sends payload.roles
function parseJwtRole(token) {
    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        const roles = payload.roles;
        const rolesStr = Array.isArray(roles) ? roles.join(",") : String(roles || "");
        return rolesStr.toUpperCase().includes("ADMIN") ? "ADMIN" : "USER";
    } catch {
        return "USER";
    }
}

// --- Persistence ---
function setUserSession(token, username) {
    const role = parseJwtRole(token);
    currentUser = { token, username, role };
    localStorage.setItem("gymbook_user", JSON.stringify(currentUser));
    updateNavUI();
    loadClasses();
}

function clearUserSession() {
    currentUser = null;
    localStorage.removeItem("gymbook_user");
    updateNavUI();
    loadClasses();
}

function loadUserFromStorage() {
    try {
        const data = localStorage.getItem("gymbook_user");
        if (data) {
            currentUser = JSON.parse(data);
            updateNavUI();
        }
    } catch {
        localStorage.removeItem("gymbook_user");
    }
}

function updateNavUI() {
    if (!currentUser) {
        navUser.classList.add("hidden");
        openAuthBtn.classList.remove("hidden");
        classIdSearchBlock.classList.add("hidden");
        return;
    }

    navUser.classList.remove("hidden");
    openAuthBtn.classList.add("hidden");
    navAvatar.textContent = currentUser.username.slice(0, 2).toUpperCase();
    navRoleBadge.textContent = currentUser.role;
    navRoleBadge.className = "role-badge " + currentUser.role.toLowerCase();

    if (isAdmin()) {
        classIdSearchBlock.classList.remove("hidden");
    } else {
        classIdSearchBlock.classList.add("hidden");
    }
}

// =========================
// MODAL TABS
// =========================
document.querySelectorAll(".mtab").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".mtab").forEach(b => b.classList.remove("active"));
        document.querySelectorAll(".tab-content").forEach(t => t.classList.add("hidden"));
        btn.classList.add("active");
        document.getElementById(`tab-${btn.dataset.tab}`).classList.remove("hidden");
    });
});

// =========================
// AUTH EVENTS
// =========================
openAuthBtn.addEventListener("click", () => authModal.classList.remove("hidden"));
closeAuthBtn.addEventListener("click", () => authModal.classList.add("hidden"));
authModal.addEventListener("click", e => { if (e.target === authModal) authModal.classList.add("hidden"); });

registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = registerForm.querySelector("button[type=submit]");
    setLoading(btn, true);
    try {
        const res = await fetch("/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: registerUsername.value.trim(), password: registerPassword.value })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || "Registration failed");
        showToast(`Account "${data.username}" created! You can now login.`, "success");
        registerForm.reset();
    } catch (err) {
        showToast(err.message, "error");
    } finally {
        setLoading(btn, false);
    }
});

loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = loginForm.querySelector("button[type=submit]");
    setLoading(btn, true);
    try {
        const res = await fetch("/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: loginUsername.value.trim(), password: loginPassword.value })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || "Login failed");
        setUserSession(data.accessToken, loginUsername.value.trim());
        authModal.classList.add("hidden");
        loginForm.reset();
        showToast(`Welcome back, ${currentUser.username}!`, "success");
    } catch (err) {
        showToast(err.message, "error");
    } finally {
        setLoading(btn, false);
    }
});

logoutButton.addEventListener("click", () => {
    clearUserSession();
    classDetailsContainer.innerHTML = '<p class="placeholder-text">Select a class to see details.</p>';
    bookingsContainer.innerHTML = '<p class="placeholder-text">Select a class to see bookings.</p>';
    document.getElementById("bookingsBlock").classList.add("hidden");
    activeBookingsClassId = null;
    showToast("Signed out.");
});

// =========================
// LOADING STATE HELPER
// =========================
function setLoading(btn, loading) {
    btn.disabled = loading;
    btn.style.opacity = loading ? "0.6" : "";
    btn.style.cursor = loading ? "not-allowed" : "";
}

// =========================
// SECTION HEADER
// =========================
function renderSectionHeader() {
    const header = document.querySelector(".section-header");
    const existing = header.querySelector(".btn-add-class");
    if (existing) existing.remove();
    if (isAdmin()) {
        const btn = document.createElement("button");
        btn.className = "btn-accent btn-add-class";
        btn.textContent = "+ Add class";
        btn.addEventListener("click", () => openAdminModal());
        header.appendChild(btn);
    }
}

// =========================
// EMAIL MASK (for non-admins)
// =========================
function maskEmail(email) {
    if (!email || !email.includes("@")) return email;
    const [local, domain] = email.split("@");
    const visible = local.slice(0, 2);
    return `${visible}***@${domain}`;
}

// =========================
// RENDER — CLASSES
// =========================
function spotsClass(n) {
    if (n === 0) return "full";
    if (n <= 3) return "low";
    return "";
}

// Safe attribute encoding
function attr(str) {
    return String(str).replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}
// Calculate next occurrence date for a given day-of-week string
// e.g. "Monday" -> "Mon 14 Apr"
function nextDateForDay(dayName) {
    const days = ["sunday","monday","tuesday","wednesday","thursday","friday","saturday"];
    const target = days.indexOf(dayName.toLowerCase());
    if (target === -1) return null;
    const today = new Date();
    const todayDay = today.getDay();
    let diff = target - todayDay;
    if (diff <= 0) diff += 7;
    const next = new Date(today);
    next.setDate(today.getDate() + diff);
    return next.toLocaleDateString("en-GB", { weekday: "short", day: "numeric", month: "short" });
}

function renderClasses(classes) {
    renderSectionHeader();

    // Show/hide bookings panel based on role
    const bookingsBlock = document.getElementById("bookingsBlock");
    if (isAdmin()) {
        bookingsBlock.classList.remove("hidden");
    } else {
        bookingsBlock.classList.add("hidden");
    }

    if (!classes || classes.length === 0) {
        classesContainer.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🏋️</div>
        <p class="empty-title">No classes found</p>
        <p class="empty-sub">Try a different filter or check back later.</p>
      </div>`;
        return;
    }

    classesContainer.innerHTML = classes.map(c => `
    <div class="gym-class-card" data-id="${c.id}">
      <div class="card-header">
        <h3 class="card-name">${c.name}</h3>
        <span class="spots-pill" id="spots-${c.id}">…</span>
      </div>
      <div class="card-meta">
        <strong>${c.instructor}</strong>
        <div class="meta-row">
          <span class="meta-chip date-chip">📅&nbsp;${nextDateForDay(c.dayOfWeek) || c.dayOfWeek}</span>
          <span class="meta-chip">🕐 ${c.startTime}</span>
          <span class="meta-chip">⏱ ${c.durationMinutes} min</span>
        </div>
      </div>
      <div class="card-actions">
        ${isLoggedIn() ? `<button class="btn-card book"
          data-action="book"
          data-id="${c.id}"
          data-name="${attr(c.name)}"
          data-instructor="${attr(c.instructor)}"
          data-day="${attr(c.dayOfWeek)}"
          data-time="${attr(c.startTime)}">Book</button>` : ""}
        <button class="btn-card" data-action="details" data-id="${c.id}">Details</button>
        ${isAdmin() ? `<button class="btn-card" data-action="bookings" data-id="${c.id}">Bookings</button>` : ""}
        ${isAdmin() ? `
          <button class="btn-card" data-action="edit" data-id="${c.id}">Edit</button>
          <button class="btn-card danger" data-action="delete" data-id="${c.id}">Delete</button>
        ` : ""}
      </div>
    </div>
  `).join("");

    // Admin "add" card
    if (isAdmin()) {
        const addCard = document.createElement("div");
        addCard.className = "add-class-card";
        addCard.dataset.action = "add";
        addCard.innerHTML = `<div class="plus-icon">+</div><span>Add new class</span>`;
        classesContainer.appendChild(addCard);
    }

    classes.forEach(c => loadSpots(c.id));
}

// =========================
// EVENT DELEGATION — classes grid
// =========================
classesContainer.addEventListener("click", async (e) => {
    const actionEl = e.target.closest("[data-action]");

    if (actionEl) {
        const action = actionEl.dataset.action;
        const id = actionEl.dataset.id;

        if (action === "add") {
            openAdminModal();
            return;
        }

        if (action === "book") {
            openBookModal(
                id,
                actionEl.dataset.name,
                actionEl.dataset.instructor,
                actionEl.dataset.day,
                actionEl.dataset.time
            );
            return;
        }

        if (action === "details") {
            await loadClassById(id);
            return;
        }

        if (action === "bookings") {
            await loadBookingsForClass(id);
            return;
        }

        if (action === "edit") {
            await prepareEditClass(id);
            return;
        }

        if (action === "delete") {
            await deleteClass(id);
            return;
        }
    }

    const card = e.target.closest(".gym-class-card");
    if (!card) return;

    const classId = card.dataset.id;
    await loadClassById(classId);
});

// Event delegation — bookings panel
bookingsContainer.addEventListener("click", async (e) => {
    const btn = e.target.closest("[data-action='delete-booking']");
    if (!btn) return;
    await deleteBooking(btn.dataset.id, activeBookingsClassId);
});

// =========================
// RENDER — CLASS DETAILS
// Clean: no ID, no booking list, just class info
// =========================
function renderClassDetails(gymClass) {
    classDetailsContainer.innerHTML = `
    <p class="detail-name">${gymClass.name}</p>
    ${gymClass.description ? `<p class="detail-desc">${gymClass.description}</p>` : ""}

    ${isAdmin() ? `<div class="detail-row"><span class="label">ID</span><span class="value">${gymClass.id}</span></div>` : ""}

    <div class="detail-row"><span class="label">Instructor</span><span class="value">${gymClass.instructor}</span></div>
    <div class="detail-row"><span class="label">Day</span><span class="value">${gymClass.dayOfWeek}</span></div>
    <div class="detail-row"><span class="label">Start time</span><span class="value">${gymClass.startTime}</span></div>
    <div class="detail-row"><span class="label">Duration</span><span class="value">${gymClass.durationMinutes} min</span></div>
    <div class="detail-row"><span class="label">Max participants</span><span class="value">${gymClass.maxParticipants}</span></div>
  `;
}

// =========================
// RENDER — BOOKINGS
// Emails masked for regular users
// =========================
function renderBookings(bookings, classId) {
    activeBookingsClassId = classId;

    if (!bookings || bookings.length === 0) {
        bookingsContainer.innerHTML = '<p class="placeholder-text">No bookings for this class yet.</p>';
        return;
    }

    bookingsContainer.innerHTML = bookings.map(b => `
    <div class="booking-item">
      <div class="booking-info">
        <p class="booking-name">${b.participantName}</p>
        <p class="booking-email">${isAdmin() ? b.email : maskEmail(b.email)}</p>
      </div>
      ${isAdmin() ? `<button class="btn-icon" data-action="delete-booking" data-id="${b.id}" title="Remove booking">&#x2715;</button>` : ""}
    </div>
  `).join("");
}

// =========================
// SPOTS
// =========================
async function loadSpots(classId) {
    try {
        const res = await fetch(`/classes/${classId}/spots-remaining`);
        if (!res.ok) throw new Error();
        const data = await res.json();
        const el = document.getElementById(`spots-${classId}`);
        if (!el) return;
        const n = data.spotsRemaining;
        el.textContent = n === 0 ? "Full" : `${n} spots`;
        el.className = `spots-pill ${spotsClass(n)}`;
    } catch {
        const el = document.getElementById(`spots-${classId}`);
        if (el) el.textContent = "?";
    }
}

// =========================
// LOAD CLASSES
// =========================
async function loadClasses() {
    renderSectionHeader();
    try {
        const res = await fetch("/classes?page=0&size=20&sort=name,asc");
        if (!res.ok) throw new Error("Failed to load classes");
        const data = await res.json();
        renderClasses(data.content);
    } catch (err) {
        classesContainer.innerHTML = '<p class="placeholder-text">Could not load classes.</p>';
        showToast(err.message, "error");
    }
}

async function loadAvailableClasses() {
    try {
        const res = await fetch("/classes/available");
        if (!res.ok) throw new Error("Failed to load available classes");
        const data = await res.json();
        renderClasses(data);
        showToast("Showing available classes.");
    } catch (err) {
        classesContainer.innerHTML = '<p class="placeholder-text">Could not load available classes.</p>';
        showToast(err.message, "error");
    }
}

async function loadClassById(classId) {
    try {
        const res = await fetch(`/classes/${classId}`);
        if (!res.ok) { const d = await res.json(); throw new Error(d.message || "Failed to load details"); }
        const data = await res.json();
        renderClassDetails(data);
        showToast(`Details: ${data.name}`);
    } catch (err) {
        classDetailsContainer.innerHTML = '<p class="placeholder-text">Could not load class details.</p>';
        showToast(err.message, "error");
    }
}

async function loadSingleClassView(classId) {
    try {
        const res = await fetch(`/classes/${classId}`);
        if (!res.ok) {
            const d = await res.json();
            throw new Error(d.message || "Failed to load class");
        }

        const data = await res.json();

        renderClassDetails(data);
        renderClasses([data]);
        showToast(`Showing class #${data.id}: ${data.name}`, "success");
    } catch (err) {
        classDetailsContainer.innerHTML = '<p class="placeholder-text">Could not load class details.</p>';
        showToast(err.message, "error");
    }
}

// Client-side search by partial instructor name (case-insensitive)
// Works regardless of whether backend uses Contains or exact match
async function searchClassesByInstructor(instructor) {
    try {
        const query = instructor.trim().toLowerCase();

        if (!query) {
            showToast("Enter an instructor name.", "error");
            return;
        }

        const res = await fetch("/classes?page=0&size=100&sort=name,asc");
        if (!res.ok) throw new Error("Failed to load classes for search");

        const data = await res.json();

        const filtered = (data.content || []).filter(c =>
            c.instructor && c.instructor.toLowerCase().includes(query)
        );

        renderClasses(filtered);

        showToast(
            filtered.length
                ? `${filtered.length} class${filtered.length > 1 ? "es" : ""} found for "${instructor}"`
                : `No classes found for "${instructor}"`
        );
    } catch (err) {
        classesContainer.innerHTML = '<p class="placeholder-text">Search failed.</p>';
        showToast(err.message, "error");
    }
}

// =========================
// ADMIN — CLASS CRUD
// =========================
function openAdminModal(gymClass = null) {
    if (!isAdmin()) { showToast("Admin access required.", "error"); return; }
    if (gymClass) {
        adminModalTitle.textContent    = "Edit class";
        adminClassId.value             = gymClass.id;
        adminName.value                = gymClass.name;
        adminInstructor.value          = gymClass.instructor;
        adminDescription.value         = gymClass.description || "";
        adminDayOfWeek.value           = gymClass.dayOfWeek;
        adminStartTime.value           = gymClass.startTime;
        adminDurationMinutes.value     = gymClass.durationMinutes;
        adminMaxParticipants.value     = gymClass.maxParticipants;
    } else {
        adminModalTitle.textContent = "Add class";
        adminClassForm.reset();
        adminClassId.value = "";
    }
    adminModal.classList.remove("hidden");
}

closeAdminBtn.addEventListener("click", () => adminModal.classList.add("hidden"));
adminModal.addEventListener("click", e => { if (e.target === adminModal) adminModal.classList.add("hidden"); });
adminResetButton.addEventListener("click", () => {
    adminClassForm.reset();
    adminClassId.value = "";
    adminModalTitle.textContent = "Add class";
});

function classBodyFromForm() {
    return {
        name:            adminName.value.trim(),
        instructor:      adminInstructor.value.trim(),
        description:     adminDescription.value.trim(),
        dayOfWeek:       adminDayOfWeek.value.trim(),
        startTime:       adminStartTime.value.trim(),
        durationMinutes: Number(adminDurationMinutes.value),
        maxParticipants: Number(adminMaxParticipants.value)
    };
}

adminClassForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const id  = adminClassId.value.trim();
    const btn = adminClassForm.querySelector("button[type=submit]");
    setLoading(btn, true);
    try {
        if (id) { await updateClass(id); } else { await createClass(); }
    } catch (err) {
        showToast(err.message, "error");
    } finally {
        setLoading(btn, false);
    }
});

async function createClass() {
    const token = getToken();
    if (!token) { showToast("ADMIN login required.", "error"); return; }
    const res = await fetch("/classes", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
        body: JSON.stringify(classBodyFromForm())
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || data.error || "Failed to create class");
    showToast(`Class "${data.name}" created!`, "success");
    adminModal.classList.add("hidden");
    await loadClasses();
}

async function updateClass(classId) {
    const token = getToken();
    if (!token) { showToast("ADMIN login required.", "error"); return; }
    const res = await fetch(`/classes/${classId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
        body: JSON.stringify(classBodyFromForm())
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || data.error || "Failed to update class");
    showToast(`Class updated!`, "success");
    adminModal.classList.add("hidden");
    renderClassDetails(data);
    await loadClasses();
}

async function prepareEditClass(classId) {
    try {
        const res = await fetch(`/classes/${classId}`);
        if (!res.ok) { const d = await res.json(); throw new Error(d.message || "Could not load class"); }
        const data = await res.json();
        openAdminModal(data);
    } catch (err) {
        showToast(err.message, "error");
    }
}

async function deleteClass(classId) {
    if (!confirm(`Delete class #${classId}? All bookings will also be removed.`)) return;
    const token = getToken();
    if (!token) { showToast("ADMIN login required.", "error"); return; }
    try {
        const res = await fetch(`/classes/${classId}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            let msg = "Failed to delete class";
            try { const d = await res.json(); msg = d.message || d.error || msg; } catch {}
            throw new Error(msg);
        }
        showToast(`Class #${classId} deleted.`, "success");
        classDetailsContainer.innerHTML = '<p class="placeholder-text">Class was deleted.</p>';
        bookingsContainer.innerHTML = '<p class="placeholder-text">Select a class to see bookings.</p>';
        activeBookingsClassId = null;
        await loadClasses();
    } catch (err) {
        showToast(err.message, "error");
    }
}

// =========================
// BOOK MODAL
// =========================
function openBookModal(classId, name, instructor, day, time) {
    if (!isLoggedIn()) {
        showToast("Please login to book a class.", "error");
        authModal.classList.remove("hidden");
        return;
    }
    bookClassIdInput.value     = classId;
    bookModalTitle.textContent = `Book: ${name}`;
    bookModalMeta.textContent  = `${instructor} · ${day} · ${time}`;
    // Auto-fill with logged-in username
    bookNameInput.value  = currentUser.username;
    bookEmailInput.value = "";
    bookModal.classList.remove("hidden");
    bookEmailInput.focus();
}

closeBookBtn.addEventListener("click", () => bookModal.classList.add("hidden"));
bookModal.addEventListener("click", e => { if (e.target === bookModal) bookModal.classList.add("hidden"); });

confirmBookBtn.addEventListener("click", async () => {
    const name  = bookNameInput.value.trim();
    const email = bookEmailInput.value.trim();

    if (!name)  { showToast("Please enter your name.", "error"); bookNameInput.focus(); return; }
    if (!email) { showToast("Please enter your email.", "error"); bookEmailInput.focus(); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showToast("Please enter a valid email address.", "error"); bookEmailInput.focus(); return;
    }

    setLoading(confirmBookBtn, true);
    try {
        await bookClass(bookClassIdInput.value);
    } catch (err) {
        showToast(err.message, "error");
    } finally {
        setLoading(confirmBookBtn, false);
    }
});

async function bookClass(classId) {
    const token = getToken();
    if (!token) {
        showToast("Please login first.", "error");
        return;
    }

    const res = await fetch(`/classes/${classId}/bookings`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            participantName: bookNameInput.value.trim(),
            email: bookEmailInput.value.trim()
        })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.message || data.error || "Booking failed");

    showToast("Booking created!", "success");
    bookModal.classList.add("hidden");

    await loadClasses();
    await loadBookingsForClass(classId, false);
}

// =========================
// BOOKINGS
// =========================
async function loadBookingsForClass(classId, showSuccessToast = true) {
    try {
        const res = await fetch(`/classes/${classId}/bookings`);
        if (!res.ok) {
            const d = await res.json();
            throw new Error(d.message || "Failed to load bookings");
        }

        const data = await res.json();
        renderBookings(data, classId);

        if (showSuccessToast) {
            showToast("Bookings loaded.");
        }
    } catch (err) {
        bookingsContainer.innerHTML = '<p class="placeholder-text">Could not load bookings.</p>';
        showToast(err.message, "error");
    }
}

async function deleteBooking(bookingId, classId) {
    if (!confirm(`Remove booking #${bookingId}?`)) return;

    const token = getToken();
    if (!token) {
        showToast("ADMIN required.", "error");
        return;
    }

    try {
        const res = await fetch(`/bookings/${bookingId}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) {
            let msg = "Failed to delete booking";
            try {
                const d = await res.json();
                msg = d.message || d.error || msg;
            } catch {}
            throw new Error(msg);
        }

        showToast("Booking deleted!", "success");
        await loadClasses();

        if (classId) {
            await loadBookingsForClass(classId, false);
        }
    } catch (err) {
        showToast(err.message, "error");
    }
}

// =========================
// SEARCH / FILTER
// =========================
searchForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const instructor = searchInstructor.value.trim();

    if (!instructor) {
        showToast("Enter an instructor name.", "error");
        return;
    }

    await searchClassesByInstructor(instructor);
});

classIdSearchForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const classId = classIdSearchInput.value.trim();

    if (!classId) {
        showToast("Enter a class ID.", "error");
        return;
    }

    await loadSingleClassView(classId);
    classIdSearchInput.value = "";
});

loadAllClassesButton.addEventListener("click", () => loadClasses());
loadAvailableClassesButton.addEventListener("click", () => loadAvailableClasses());

// =========================
// EXTRA STYLES (empty state + detail desc)
// =========================
const extraStyle = document.createElement("style");
extraStyle.textContent = `
  .empty-state {
    grid-column: 1 / -1;
    text-align: center;
    padding: 48px 24px;
  }
  .empty-icon  { font-size: 40px; margin-bottom: 12px; }
  .empty-title { font-size: 15px; font-weight: 600; color: var(--text-soft); margin: 0 0 6px; }
  .empty-sub   { font-size: 13px; color: var(--text-muted); margin: 0; }
  .detail-desc {
    font-size: 13px;
    color: var(--text-soft);
    font-style: italic;
    margin: -4px 0 12px;
    line-height: 1.5;
  }
`;
document.head.appendChild(extraStyle);

// =========================
// INIT
// =========================
loadUserFromStorage();
loadClasses();