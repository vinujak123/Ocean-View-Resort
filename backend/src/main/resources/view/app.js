const API_BASE = 'http://localhost:8081/api/reservations';

// Authentication
const loginForm = document.getElementById('loginForm');
const loginScreen = document.getElementById('loginScreen');
const mainApp = document.getElementById('mainApp');
const loginError = document.getElementById('loginError');
const logoutBtn = document.getElementById('logoutBtn');
const usernameDisplay = document.getElementById('usernameDisplay');

// Initial check for login state will be at the bottom of the script

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('http://localhost:8081/api/auth', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (data.success) {
            localStorage.setItem('oceanview_logged_in', 'true');
            localStorage.setItem('oceanview_username', data.username);
            localStorage.setItem('oceanview_role', data.role);
            showMainApp();
        } else {
            loginError.textContent = data.message || 'Invalid username or password';
            loginError.style.display = 'block';
            document.getElementById('password').value = '';
        }
    } catch (error) {
        console.error('Login error:', error);
        loginError.textContent = 'Server connection failed';
        loginError.style.display = 'block';
    }
});

logoutBtn.addEventListener('click', () => {
    localStorage.removeItem('oceanview_logged_in');
    localStorage.removeItem('oceanview_username');
    localStorage.removeItem('oceanview_role');
    loginScreen.style.display = 'flex';
    mainApp.style.display = 'none';
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    loginError.style.display = 'none';
});

function showMainApp() {
    loginScreen.style.display = 'none';
    mainApp.style.display = 'flex';
    const username = localStorage.getItem('oceanview_username') || 'User';
    const role = localStorage.getItem('oceanview_role') || 'STAFF';
    usernameDisplay.textContent = username;
    document.getElementById('avatarLetter').textContent = username.charAt(0).toUpperCase();
    document.getElementById('pageSubtitle').textContent = `Logged in as ${role === 'ADMIN' ? 'Administrator' : 'Staff Member'}`;

    // Role-based visibility (only for nav items)
    const adminNavs = document.querySelectorAll('.nav-item.admin-only');
    adminNavs.forEach(el => {
        el.style.display = (role === 'ADMIN') ? 'flex' : 'none';
    });

    // Default to dashboard
    Object.values(pages).forEach(p => { if (p) p.style.display = 'none'; });
    if (pages.dashboard) pages.dashboard.style.display = 'block';

    fetchData();
}

// Navigation
const navItems = document.querySelectorAll('.nav-item[data-page]');
const pages = {
    dashboard: document.getElementById('dashboardPage'),
    add: document.getElementById('addPage'),
    list: document.getElementById('listPage'),
    billing: document.getElementById('billingPage'),
    staff: document.getElementById('staffPage'),
    reports: document.getElementById('reportsPage'),
    help: document.getElementById('helpPage')
};
const pageTitle = document.getElementById('pageTitle');

navItems.forEach(item => {
    item.addEventListener('click', () => {
        const page = item.dataset.page;
        const role = localStorage.getItem('oceanview_role');

        // Security check for frontend navigation
        if (item.classList.contains('admin-only') && role !== 'ADMIN') {
            alert('Access Denied');
            return;
        }

        // Update active nav item
        navItems.forEach(nav => nav.classList.remove('active'));
        item.classList.add('active');

        // Show selected page
        Object.values(pages).forEach(p => { if (p) p.style.display = 'none'; });
        if (pages[page]) pages[page].style.display = 'block';

        // Update title and subtitle
        const title = page.charAt(0).toUpperCase() + page.slice(1);
        pageTitle.textContent = title === 'Add' ? 'New Booking' : (title === 'List' ? 'Reservations' : (title === 'Billing' ? 'Payments' : title));

        const subtitles = {
            dashboard: 'Overview of resort performance and activity.',
            add: 'Create a new guest record and room reservation.',
            list: 'View and manage all guest reservations.',
            billing: 'Search records and generate official invoices.',
            staff: 'Manage system users and access levels.',
            reports: 'Detailed financial analytics and performance data.',
            help: 'Guidelines and documentation for system usage.'
        };
        document.getElementById('pageSubtitle').textContent = subtitles[page] || '';

        // Fetch specialized data
        if (page === 'list') fetchReservations();
        if (page === 'staff') fetchStaffList();
        if (page === 'reports') fetchReportData();
    });
});

// Exit System
const exitBtn = document.getElementById('exitBtn');
const exitOverlay = document.getElementById('exitOverlay');
const cancelExit = document.getElementById('cancelExit');
const confirmExit = document.getElementById('confirmExit');

exitBtn.addEventListener('click', () => {
    exitOverlay.style.display = 'flex';
});

cancelExit.addEventListener('click', () => {
    exitOverlay.style.display = 'none';
});

confirmExit.addEventListener('click', () => {
    // Graceful exit: logout and redirect or show goodbye
    localStorage.removeItem('oceanview_logged_in');
    localStorage.removeItem('oceanview_username');
    localStorage.removeItem('oceanview_role');

    document.body.innerHTML = `
        <div style="height: 100vh; display: flex; align-items: center; justify-content: center; background: #0f172a; color: white; flex-direction: column; font-family: 'Outfit', sans-serif;">
            <h1 style="font-size: 3rem; margin-bottom: 1rem;">Session Ended</h1>
            <p style="color: #94a3b8; margin-bottom: 2rem;">You have safely exited the Ocean View Resort Management System.</p>
            <button onclick="location.reload()" style="background: white; color: #0f172a; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer;">Return to Login</button>
        </div>
    `;
});

// Fetch data
async function fetchData() {
    try {
        const [reservations, stats] = await Promise.all([
            fetch(API_BASE).then(r => r.json()),
            fetch(`${API_BASE}/stats`).then(r => r.json())
        ]);

        updateStats(stats);
        updateReservationsList(reservations);
    } catch (error) {
        console.error('Error fetching data:', error);
    }
}

function updateStats(stats) {
    document.getElementById('totalBookings').textContent = stats.totalBookings;
    document.getElementById('totalRevenue').textContent = `LKR ${stats.totalRevenue.toLocaleString()}`;
    document.getElementById('occupancyRate').textContent = stats.occupancyRate;
}

async function fetchReservations() {
    try {
        const reservations = await fetch(API_BASE).then(r => r.json());
        updateReservationsList(reservations);
    } catch (error) {
        console.error('Error fetching reservations:', error);
    }
}

function updateReservationsList(reservations) {
    const tbody = document.getElementById('reservationsBody');
    tbody.innerHTML = '';

    reservations.forEach(r => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="ref-id">#${r.referenceId}</td>
            <td><strong>${r.guestName}</strong></td>
            <td>
                <div class="plan-info">
                    <span class="room-type">${r.roomType}</span>
                    <span class="board-type">${r.boardType}</span>
                </div>
            </td>
            <td style="color: #64748b; font-size: 14px;">${r.checkInDate} to ${r.checkOutDate}</td>
            <td><strong>LKR ${r.totalBill?.toLocaleString()}</strong></td>
            <td>
                <div style="display: flex; gap: 8px;">
                    <button class="btn-edit" onclick="openEditModal('${r.referenceId}')">Edit</button>
                    <button class="btn-delete" onclick="deleteReservation('${r.referenceId}')">Delete</button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Reservation Form
const reservationForm = document.getElementById('reservationForm');
const roomTypeSelect = document.getElementById('roomType');
const boardTypeSelect = document.getElementById('boardType');
const checkInInput = document.getElementById('checkInDate');
const checkOutInput = document.getElementById('checkOutDate');
const estimateBox = document.getElementById('estimateBox');
const estimateAmount = document.getElementById('estimateAmount');

function calculateEstimate() {
    const checkIn = checkInInput.value;
    const checkOut = checkOutInput.value;

    if (!checkIn || !checkOut) {
        estimateBox.style.display = 'none';
        return;
    }

    const start = new Date(checkIn);
    const end = new Date(checkOut);
    const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));

    if (nights <= 0) {
        estimateBox.style.display = 'none';
        return;
    }

    const roomRates = { STANDARD: 15000, DELUXE: 25000, SUITE: 45000 };
    const boardRates = { BB: 0, HB: 5000, FB: 10000 };

    const total = nights * (roomRates[roomTypeSelect.value] + boardRates[boardTypeSelect.value]);

    estimateAmount.textContent = `LKR ${total.toLocaleString()}`;
    estimateBox.style.display = 'flex';
}

roomTypeSelect.addEventListener('change', calculateEstimate);
boardTypeSelect.addEventListener('change', calculateEstimate);
checkInInput.addEventListener('change', calculateEstimate);
checkOutInput.addEventListener('change', calculateEstimate);

// Edit Modal Logic
const editModal = document.getElementById('editModal');
const closeEditModal = document.getElementById('closeEditModal');
const editReservationForm = document.getElementById('editReservationForm');
const editRoomTypeSelect = document.getElementById('editRoomType');
const editBoardTypeSelect = document.getElementById('editBoardType');
const editCheckInInput = document.getElementById('editCheckInDate');
const editCheckOutInput = document.getElementById('editCheckOutDate');
const editEstimateBox = document.getElementById('editEstimateBox');
const editEstimateAmount = document.getElementById('editEstimateAmount');

function calculateEditEstimate() {
    const checkIn = editCheckInInput.value;
    const checkOut = editCheckOutInput.value;

    if (!checkIn || !checkOut) {
        editEstimateBox.style.display = 'none';
        return;
    }

    const start = new Date(checkIn);
    const end = new Date(checkOut);
    const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));

    if (nights <= 0) {
        editEstimateBox.style.display = 'none';
        return;
    }

    const roomRates = { STANDARD: 15000, DELUXE: 25000, SUITE: 45000 };
    const boardRates = { BB: 0, HB: 5000, FB: 10000 };

    const total = nights * (roomRates[editRoomTypeSelect.value] + boardRates[editBoardTypeSelect.value]);

    editEstimateAmount.textContent = `LKR ${total.toLocaleString()}`;
    editEstimateBox.style.display = 'flex';
}

editRoomTypeSelect.addEventListener('change', calculateEditEstimate);
editBoardTypeSelect.addEventListener('change', calculateEditEstimate);
editCheckInInput.addEventListener('change', calculateEditEstimate);
editCheckOutInput.addEventListener('change', calculateEditEstimate);

window.openEditModal = async (refId) => {
    try {
        const response = await fetch(`${API_BASE}/${refId}`);
        if (!response.ok) throw new Error('Failed to fetch reservation');
        const r = await response.json();

        document.getElementById('editRefId').textContent = `#${r.referenceId}`;
        document.getElementById('editGuestName').value = r.guestName;
        document.getElementById('editPhone').value = r.phone;
        document.getElementById('editAddress').value = r.address || '';
        editRoomTypeSelect.value = r.roomType;
        editBoardTypeSelect.value = r.boardType;
        editCheckInInput.value = r.checkInDate;
        editCheckOutInput.value = r.checkOutDate;

        calculateEditEstimate();
        editModal.style.display = 'flex';
    } catch (error) {
        console.error('Error opening edit modal:', error);
        alert('Error loading reservation details.');
    }
};

closeEditModal.addEventListener('click', () => {
    editModal.style.display = 'none';
});

editReservationForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const refId = document.getElementById('editRefId').textContent.substring(1);

    const formData = {
        guestName: document.getElementById('editGuestName').value,
        address: document.getElementById('editAddress').value,
        phone: document.getElementById('editPhone').value,
        roomType: editRoomTypeSelect.value,
        boardType: editBoardTypeSelect.value,
        checkInDate: editCheckInInput.value,
        checkOutDate: editCheckOutInput.value
    };

    try {
        const response = await fetch(`${API_BASE}/${refId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            alert('Reservation Updated Successfully!');
            editModal.style.display = 'none';
            fetchData();
        } else {
            const error = await response.json();
            alert(`Error: ${error.message || 'Failed to update reservation'}`);
        }
    } catch (error) {
        console.error('Error updating reservation:', error);
        alert('Error updating reservation. Please try again.');
    }
});

window.deleteReservation = async (refId) => {
    if (!confirm(`Are you sure you want to delete reservation #${refId}?`)) return;

    try {
        const response = await fetch(`${API_BASE}/${refId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            alert('Reservation Deleted Successfully!');
            fetchData();
        } else {
            const error = await response.json();
            alert(`Error: ${error.message || 'Failed to delete reservation'}`);
        }
    } catch (error) {
        console.error('Error deleting reservation:', error);
        alert('Error deleting reservation. Please try again.');
    }
};

reservationForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = {
        guestName: document.getElementById('guestName').value,
        address: document.getElementById('address').value,
        phone: document.getElementById('phone').value,
        roomType: roomTypeSelect.value,
        boardType: boardTypeSelect.value,
        checkInDate: checkInInput.value,
        checkOutDate: checkOutInput.value
    };

    try {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            alert('Reservation Created Successfully!');
            reservationForm.reset();
            estimateBox.style.display = 'none';
            fetchData();

            // Switch to list view
            navItems.forEach(nav => nav.classList.remove('active'));
            document.querySelector('[data-page="list"]').classList.add('active');
            Object.values(pages).forEach(p => p.style.display = 'none');
            pages.list.style.display = 'block';
            pageTitle.textContent = 'List';
        } else {
            const error = await response.json();
            alert(`Error: ${error.message || 'Failed to create reservation'}`);
        }
    } catch (error) {
        console.error('Error creating reservation:', error);
        alert('Error creating reservation. Please try again.');
    }
});

// Staff Management
async function fetchStaffList() {
    const role = localStorage.getItem('oceanview_role');
    try {
        const response = await fetch('http://localhost:8081/api/users', {
            headers: { 'X-Role': role }
        });
        if (response.ok) {
            const users = await response.json();
            renderStaffTable(users);
        }
    } catch (e) { console.error('Error fetching staff:', e); }
}

function renderStaffTable(users) {
    const tbody = document.getElementById('staffTableBody');
    tbody.innerHTML = '';
    users.forEach(u => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${u.username}</td>
            <td><span class="badge ${u.role === 'ADMIN' ? 'admin' : 'staff'}">${u.role}</span></td>
            <td>
                ${u.username !== 'admin' ? `<button class="btn-delete" onclick="deleteUser('${u.username}')">Delete</button>` : 'System User'}
            </td>
        `;
        tbody.appendChild(row);
    });
}

document.getElementById('staffForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('newStaffUsername').value;
    const password = document.getElementById('newStaffPassword').value;
    const role = document.getElementById('newStaffRole').value;
    const adminRole = localStorage.getItem('oceanview_role');

    try {
        const response = await fetch('http://localhost:8081/api/users', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Role': adminRole
            },
            body: JSON.stringify({ username, password, role })
        });
        if (response.ok) {
            alert('Staff account created!');
            document.getElementById('staffForm').reset();
            fetchStaffList();
        } else {
            const err = await response.json();
            alert(err.message);
        }
    } catch (e) { alert('Failed to create account'); }
});

async function deleteUser(username) {
    if (!confirm(`Are you sure you want to delete ${username}?`)) return;
    const role = localStorage.getItem('oceanview_role');
    try {
        const response = await fetch(`http://localhost:8081/api/users?username=${username}`, {
            method: 'DELETE',
            headers: { 'X-Role': role }
        });
        if (response.ok) fetchStaffList();
    } catch (e) { alert('Delete failed'); }
}

// Billing
const generateInvoiceBtn = document.getElementById('generateInvoiceBtn');
const searchRefIdInput = document.getElementById('searchRefId');
const invoiceContainer = document.getElementById('invoiceContainer');
const invoiceDetails = document.getElementById('invoiceDetails');

generateInvoiceBtn.addEventListener('click', async () => {
    const refId = searchRefIdInput.value.trim();
    if (!refId) {
        alert('Please enter a reference ID');
        return;
    }
    try {
        const response = await fetch(`${API_BASE}/${refId}`);
        if (response.ok) {
            const bill = await response.json();
            displayInvoice(bill);
        } else {
            alert('Reservation not found');
            invoiceContainer.style.display = 'none';
        }
    } catch (error) {
        console.error('Error fetching invoice:', error);
        alert('Error fetching invoice. Please try again.');
    }
});

function displayInvoice(bill) {
    invoiceDetails.innerHTML = `
        <span class="invoice-label">ID:</span>
        <span class="invoice-value" style="color: #2563eb;">#${bill.referenceId}</span>
        <span class="invoice-label">Guest:</span>
        <span class="invoice-value" style="text-transform: uppercase;">${bill.guestName}</span>
        <span class="invoice-label">Stay:</span>
        <span class="invoice-value">${bill.checkInDate} — ${bill.checkOutDate}</span>
        <span class="invoice-label">Room:</span>
        <span class="invoice-value" style="text-decoration: underline; text-decoration-color: #2563eb; text-decoration-thickness: 2px;">${bill.roomType}</span>
    `;

    const totalDiv = document.createElement('div');
    totalDiv.className = 'invoice-total';
    totalDiv.innerHTML = `
        <span class="total-label">Total Due</span>
        <span class="total-amount">LKR ${bill.totalBill?.toLocaleString()}</span>
    `;

    invoiceDetails.parentElement.insertBefore(totalDiv, invoiceDetails.nextSibling);
    invoiceContainer.style.display = 'block';
}

// Financial Reports
async function fetchReportData() {
    const role = localStorage.getItem('oceanview_role');
    try {
        const [statsRes, reservationsRes] = await Promise.all([
            fetch(`${API_BASE}/stats`, { headers: { 'X-Role': role } }),
            fetch(API_BASE)
        ]);
        if (!statsRes.ok) return;
        const stats = await statsRes.json();
        const reservations = reservationsRes.ok ? await reservationsRes.json() : [];

        const fmt = n => `LKR ${Number(n).toLocaleString()}`;
        const totalRevenue = stats.totalRevenue || 0;
        const totalBookings = stats.totalBookings || 0;

        // ── Hero metrics ─────────────────────────────────
        document.getElementById('reportRevenue').textContent = fmt(totalRevenue);
        document.getElementById('reportBookings').textContent = totalBookings;
        document.getElementById('reportNights').textContent = stats.totalNights || 0;
        document.getElementById('reportAvgStay').textContent = stats.avgStayNights || 0;
        document.getElementById('reportAvgBill').textContent = fmt(stats.avgBill || 0);
        document.getElementById('reportGenDate').textContent =
            new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'long', year: 'numeric' })
            + ' · ' + new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
        document.getElementById('reportTotalTag').textContent = `${totalBookings} reservation${totalBookings !== 1 ? 's' : ''}`;

        // Top guest caption under avg bill metric
        if (stats.topGuest) {
            document.getElementById('reportTopGuestLine').textContent = `Top guest: ${stats.topGuest}`;
        }

        // ── Room type — DONUT ────────────────────────────
        const revenueByRoom = stats.revenueByRoom || {};
        const countByRoom = stats.countByRoom || {};
        const roomColors = { STANDARD: '#2563eb', DELUXE: '#7c3aed', SUITE: '#0891b2' };
        const roomLabels = { STANDARD: 'Standard', DELUXE: 'Deluxe', SUITE: 'Suite' };

        document.getElementById('reportRoomTotal').textContent = fmt(totalRevenue);
        document.getElementById('donutTotal').textContent = totalBookings;

        // Build SVG donut arcs
        const donutSvg = document.getElementById('donutSvg');
        // keep the base circle (first child), remove arcs
        while (donutSvg.children.length > 1) donutSvg.removeChild(donutSvg.lastChild);

        const R = 48, CX = 60, CY = 60;
        const circumference = 2 * Math.PI * R;
        let offset = 0;
        const roomKeys = Object.keys(revenueByRoom);

        roomKeys.forEach(key => {
            const rev = revenueByRoom[key] || 0;
            const pct = totalRevenue > 0 ? rev / totalRevenue : 0;
            const dashLen = pct * circumference;
            if (dashLen === 0) return;
            const arc = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            arc.setAttribute('cx', CX);
            arc.setAttribute('cy', CY);
            arc.setAttribute('r', R);
            arc.setAttribute('fill', 'none');
            arc.setAttribute('stroke', roomColors[key] || '#94a3b8');
            arc.setAttribute('stroke-width', '16');
            arc.setAttribute('stroke-dasharray', `${dashLen} ${circumference - dashLen}`);
            arc.setAttribute('stroke-dashoffset', -offset);
            arc.setAttribute('transform', `rotate(-90 ${CX} ${CY})`);
            arc.style.transition = 'stroke-dasharray 0.8s ease';
            donutSvg.appendChild(arc);
            offset += dashLen;
        });

        // Legend
        const legendEl = document.getElementById('reportRoomLegend');
        legendEl.innerHTML = '';
        roomKeys.forEach(key => {
            const rev = revenueByRoom[key] || 0;
            const cnt = countByRoom[key] || 0;
            const pct = totalRevenue > 0 ? Math.round((rev / totalRevenue) * 100) : 0;
            legendEl.innerHTML += `
              <div class="rp-legend-item">
                <span class="rp-legend-dot" style="background:${roomColors[key] || '#94a3b8'}"></span>
                <div class="rp-legend-body">
                  <span class="rp-legend-name">${roomLabels[key] || key}</span>
                  <span class="rp-legend-cnt">${cnt} booking${cnt !== 1 ? 's' : ''} · ${pct}%</span>
                </div>
                <span class="rp-legend-rev">${fmt(rev)}</span>
              </div>`;
        });

        // ── Board plan — HORIZONTAL BARS ─────────────────
        const revenueByBoard = stats.revenueByBoard || {};
        const countByBoard = stats.countByBoard || {};
        const boardColors = { BB: '#059669', HB: '#d97706', FB: '#e11d48' };
        const boardLabels = { BB: 'Bed & Breakfast', HB: 'Half Board', FB: 'Full Board' };

        document.getElementById('reportBoardTotal').textContent = fmt(totalRevenue);
        const boardEl = document.getElementById('reportBoardBreakdown');
        boardEl.innerHTML = '';

        const maxBoardRev = Math.max(...Object.values(revenueByBoard));
        Object.keys(revenueByBoard).forEach(key => {
            const rev = revenueByBoard[key] || 0;
            const cnt = countByBoard[key] || 0;
            const pct = totalRevenue > 0 ? Math.round((rev / totalRevenue) * 100) : 0;
            const barW = maxBoardRev > 0 ? Math.round((rev / maxBoardRev) * 100) : 0;
            boardEl.innerHTML += `
              <div class="rp-bar-row">
                <div class="rp-bar-meta">
                  <span class="rp-bar-label">${boardLabels[key] || key}</span>
                  <span class="rp-bar-count">${cnt} booking${cnt !== 1 ? 's' : ''}</span>
                </div>
                <div class="rp-bar-track">
                  <div class="rp-bar-fill" style="width:${barW}%;background:${boardColors[key] || '#64748b'}"></div>
                </div>
                <div class="rp-bar-stats">
                  <span class="rp-bar-pct">${pct}%</span>
                  <span class="rp-bar-rev">${fmt(rev)}</span>
                </div>
              </div>`;
        });

        // ── Recent Reservations Table ─────────────────────
        const tbody = document.getElementById('reportTableBody');
        tbody.innerHTML = '';
        if (reservations.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="rp-empty">No reservations found</td></tr>';
        } else {
            [...reservations].reverse().slice(0, 10).forEach(r => {
                const roomLabel = roomLabels[r.roomType] || r.roomType || '—';
                const boardLabel = boardLabels[r.boardType] || r.boardType || '—';
                tbody.innerHTML += `
                  <tr>
                    <td class="rp-td-ref">#${r.referenceId}</td>
                    <td class="rp-td-guest">${r.guestName || '—'}</td>
                    <td>
                      <span class="rp-room-badge">${roomLabel}</span>
                      <span class="rp-board-badge">${boardLabel}</span>
                    </td>
                    <td class="rp-td-date">${r.checkInDate || '—'}</td>
                    <td class="rp-td-date">${r.checkOutDate || '—'}</td>
                    <td class="rp-td-amt">${fmt(r.totalBill || 0)}</td>
                  </tr>`;
            });
        }

        // ── Top Guest Banner ──────────────────────────────
        const guestCard = document.getElementById('reportTopGuestCard');
        if (stats.topGuest) {
            document.getElementById('reportTopGuest').textContent = stats.topGuest;
            document.getElementById('reportTopGuestSpend').textContent = fmt(stats.topGuestSpend || 0);
            guestCard.style.display = 'flex';
        } else {
            guestCard.style.display = 'none';
        }

    } catch (e) { console.error('Error fetching report:', e); }
}

// Initialize login state
if (localStorage.getItem('oceanview_logged_in') === 'true') {
    showMainApp();
}


// Initialize login state
if (localStorage.getItem('oceanview_logged_in') === 'true') {
    showMainApp();
}


// Initialize login state
if (localStorage.getItem('oceanview_logged_in') === 'true') {
    showMainApp();
}
