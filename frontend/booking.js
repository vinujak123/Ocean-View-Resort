const API_BASE = 'http://localhost:8081/api/reservations';

const bookingForm = document.getElementById('publicBookingForm');
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

// Set min dates to today
const today = new Date().toISOString().split('T')[0];
checkInInput.min = today;
checkInInput.addEventListener('change', () => {
    checkOutInput.min = checkInInput.value;
});

bookingForm.addEventListener('submit', async (e) => {
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
            const result = await response.json();
            alert(`Booking Successful!\nYour Reference ID: #${result.referenceId}\n\nWe look forward to welcoming you to Ocean View Resort.`);
            window.location.href = 'index.html';
        } else {
            const error = await response.json();
            alert(`Error: ${error.message || 'Failed to complete booking'}`);
        }
    } catch (error) {
        console.error('Error creating booking:', error);
        alert('There was an issue connecting to our reservation system. Please try again later.');
    }
});
