const API_BASE = 'http://localhost:8081/api/reservations';

// Authentication
const loginForm = document.getElementById('loginForm');
const loginScreen = document.getElementById('loginScreen');
const mainApp = document.getElementById('mainApp');
const loginError = document.getElementById('loginError');
const logoutBtn = document.getElementById('logoutBtn');
const usernameDisplay = document.getElementById('usernameDisplay');

// Check if already logged in
if (localStorage.getItem('oceanview_logged_in') === 'true') {
    showMainApp();
}

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
    usernameDisplay.textContent = `${username} (${role})`;

    // Role-based visibility
    const adminElements = document.querySelectorAll('.admin-only');
    adminElements.forEach(el => {
        el.style.display = (role === 'ADMIN') ? '' : 'none';
    });

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
    reports: document.getElementById('reportsPage')
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

        // Update title
        pageTitle.textContent = page.charAt(0).toUpperCase() + page.slice(1);

        // Fetch specialized data
        if (page === 'list') fetchReservations();
        if (page === 'staff') fetchStaffList();
        if (page === 'reports') fetchReportData();
    });
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
        const response = await fetch(`${API_BASE}/stats`, {
            headers: { 'X-Role': role }
        });
        if (response.ok) {
            const stats = await response.json();
            document.getElementById('reportRevenue').textContent = `LKR ${stats.totalRevenue.toLocaleString()}`;
            document.getElementById('reportBookings').textContent = stats.totalBookings;
            document.getElementById('reportOccupancy').textContent = stats.occupancyRate;
        }
    } catch (e) { console.error('Error fetching report:', e); }
}
