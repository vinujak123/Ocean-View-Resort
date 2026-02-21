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

loginForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (username === 'admin' && password === 'admin123') {
        localStorage.setItem('oceanview_logged_in', 'true');
        localStorage.setItem('oceanview_username', username);
        showMainApp();
    } else {
        loginError.textContent = 'Invalid username or password';
        loginError.style.display = 'block';
        document.getElementById('password').value = '';
    }
});

logoutBtn.addEventListener('click', () => {
    localStorage.removeItem('oceanview_logged_in');
    localStorage.removeItem('oceanview_username');
    loginScreen.style.display = 'flex';
    mainApp.style.display = 'none';
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    loginError.style.display = 'none';
});

function showMainApp() {
    loginScreen.style.display = 'none';
    mainApp.style.display = 'flex';
    const username = localStorage.getItem('oceanview_username') || 'Administrator';
    usernameDisplay.textContent = username;
    fetchData();
}

// Navigation
const navItems = document.querySelectorAll('.nav-item[data-page]');
const pages = {
    dashboard: document.getElementById('dashboardPage'),
    add: document.getElementById('addPage'),
    list: document.getElementById('listPage'),
    billing: document.getElementById('billingPage')
};
const pageTitle = document.getElementById('pageTitle');

navItems.forEach(item => {
    item.addEventListener('click', () => {
        const page = item.dataset.page;

        // Update active nav item
        navItems.forEach(nav => nav.classList.remove('active'));
        item.classList.add('active');

        // Show selected page
        Object.values(pages).forEach(p => p.style.display = 'none');
        pages[page].style.display = 'block';

        // Update title
        pageTitle.textContent = page.charAt(0).toUpperCase() + page.slice(1);

        // Fetch data for list page
        if (page === 'list') {
            fetchReservations();
        }
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
