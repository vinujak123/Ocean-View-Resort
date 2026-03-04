const API_BASE = 'http://localhost:8081/api/reservations';

const searchForm = document.getElementById('searchForm');
const refIdInput = document.getElementById('refIdInput');
const searchError = document.getElementById('searchError');
const manageSection = document.getElementById('manageSection');

// Form Elements
const displayRefId = document.getElementById('displayRefId');
const updateForm = document.getElementById('updateForm');
const guestNameInput = document.getElementById('guestName');
const phoneInput = document.getElementById('phone');
const addressInput = document.getElementById('address');
const roomTypeSelect = document.getElementById('roomType');
const boardTypeSelect = document.getElementById('boardType');
const checkInInput = document.getElementById('checkInDate');
const checkOutInput = document.getElementById('checkOutDate');
const estimateAmount = document.getElementById('estimateAmount');
const btnCancelBooking = document.getElementById('btnCancelBooking');

const verifyPhoneInput = document.getElementById('verifyPhoneInput');

let currentReservation = null;

// Search for Reservation
searchForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const refId = refIdInput.value.trim();
    const verifyPhone = verifyPhoneInput.value.trim();
    if (!refId || !verifyPhone) return;

    try {
        const response = await fetch(`${API_BASE}/${refId}`);
        if (response.ok) {
            const res = await response.json();

            // Security Verification Check
            if (res.phone && res.phone.trim() === verifyPhone) {
                currentReservation = res;
                populateForm(currentReservation);
                searchError.style.display = 'none';
                manageSection.style.display = 'block';
            } else {
                searchError.textContent = 'Verification failed. Phone number does not match our records.';
                searchError.style.display = 'block';
                manageSection.style.display = 'none';
            }
        } else {
            searchError.textContent = 'Reservation not found. Please check your reference ID.';
            searchError.style.display = 'block';
            manageSection.style.display = 'none';
        }
    } catch (error) {
        console.error('Error fetching reservation:', error);
        searchError.textContent = 'Server error. Please try again later.';
        searchError.style.display = 'block';
    }
});

// Populate the Edit Form
function populateForm(res) {
    displayRefId.textContent = `#${res.referenceId}`;
    guestNameInput.value = res.guestName;
    phoneInput.value = res.phone;
    addressInput.value = res.address || '';
    roomTypeSelect.value = res.roomType;
    boardTypeSelect.value = res.boardType;
    checkInInput.value = res.checkInDate;
    checkOutInput.value = res.checkOutDate;

    // Set min date to prevent booking in the past
    // However, if the booking already started, we must allow the current date.
    // For simplicity, we just set the min of checkout to the checkin date.
    checkOutInput.min = checkInInput.value;

    calculateEstimate();
}

// Pricing Calculation
function calculateEstimate() {
    const checkIn = checkInInput.value;
    const checkOut = checkOutInput.value;

    if (!checkIn || !checkOut) return;

    const start = new Date(checkIn);
    const end = new Date(checkOut);
    const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));

    if (nights <= 0) {
        estimateAmount.textContent = 'Invalid Dates';
        return;
    }

    const roomRates = { STANDARD: 15000, DELUXE: 25000, SUITE: 45000 };
    const boardRates = { BB: 0, HB: 5000, FB: 10000 };

    const total = nights * (roomRates[roomTypeSelect.value] + boardRates[boardTypeSelect.value]);
    estimateAmount.textContent = `LKR ${total.toLocaleString()}`;
}

// Auto-recalculate on change
roomTypeSelect.addEventListener('change', calculateEstimate);
boardTypeSelect.addEventListener('change', calculateEstimate);
checkInInput.addEventListener('change', () => {
    checkOutInput.min = checkInInput.value;
    calculateEstimate();
});
checkOutInput.addEventListener('change', calculateEstimate);

// Update Booking Logic (PUT)
updateForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!currentReservation) return;

    const refId = currentReservation.referenceId;
    const formData = {
        guestName: guestNameInput.value,
        address: addressInput.value,
        phone: phoneInput.value,
        roomType: roomTypeSelect.value,
        boardType: boardTypeSelect.value,
        checkInDate: checkInInput.value,
        checkOutDate: checkOutInput.value
    };

    try {
        const response = await fetch(`${API_BASE}/${refId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            alert('Your reservation has been successfully updated!');
            // Re-fetch to ensure sync with server bill calculation
            const updatedRes = await response.json();
            populateForm(updatedRes);
        } else {
            const error = await response.json();
            alert(`Error: ${error.message || 'Failed to update reservation'}`);
        }
    } catch (error) {
        console.error('Error updating reservation:', error);
        alert('There was a problem communicating with the server.');
    }
});

// Cancel Booking Logic (DELETE)
btnCancelBooking.addEventListener('click', async () => {
    if (!currentReservation) return;
    const refId = currentReservation.referenceId;

    const confirmed = confirm(`Are you sure you want to completely CANCEL reservation #${refId}?\nThis action cannot be undone.`);
    if (!confirmed) return;

    try {
        const response = await fetch(`${API_BASE}/${refId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            alert('Your reservation has been successfully cancelled.');
            manageSection.style.display = 'none';
            searchForm.reset();
            currentReservation = null;
        } else {
            const error = await response.json();
            alert(`Error: ${error.message || 'Failed to cancel reservation'}`);
        }
    } catch (error) {
        console.error('Error deleting reservation:', error);
        alert('There was a problem communicating with the server.');
    }
});
