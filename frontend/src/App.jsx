import React, { useState, useEffect } from 'react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import {
    LayoutDashboard, PlusCircle, CreditCard, List, HelpCircle,
    LogOut, Phone, MapPin, User, Calendar, Lock
} from 'lucide-react';

const API_BASE = 'http://localhost:8081/api/reservations';

export default function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [nav, setNav] = useState('dashboard');
    const [reservations, setReservations] = useState([]);
    const [stats, setStats] = useState({ totalBookings: 0, totalRevenue: 0, occupancyRate: '0%' });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Check if user is logged in
        const loggedIn = localStorage.getItem('oceanview_logged_in') === 'true';
        setIsLoggedIn(loggedIn);
        if (loggedIn) {
            fetchData();
        }
    }, []);

    const fetchData = async () => {
        try {
            const [resData, statData] = await Promise.all([
                fetch(API_BASE).then(r => r.json()),
                fetch(`${API_BASE}/stats`).then(r => r.json())
            ]);
            setReservations(resData);
            setStats(statData);
            setLoading(false);
        } catch (e) {
            console.error("API error:", e);
        }
    };

    const handleLogin = (username, password) => {
        // Simple authentication - in production, this should be server-side
        if (username === 'admin' && password === 'admin123') {
            localStorage.setItem('oceanview_logged_in', 'true');
            localStorage.setItem('oceanview_username', username);
            setIsLoggedIn(true);
            fetchData();
            return true;
        }
        return false;
    };

    const handleLogout = () => {
        localStorage.removeItem('oceanview_logged_in');
        localStorage.removeItem('oceanview_username');
        setIsLoggedIn(false);
        setNav('dashboard');
    };

    if (!isLoggedIn) {
        return <LoginScreen onLogin={handleLogin} />;
    }

    return (
        <div className="flex min-h-screen bg-slate-50">
            {/* Sidebar */}
            <aside className="w-64 bg-slate-900 text-slate-400 p-6 flex flex-col">
                <h1 className="text-white text-2xl font-bold mb-10 flex items-center gap-2">
                    <div className="w-8 h-8 bg-blue-600 rounded-lg"></div>
                    OCEAN VIEW
                </h1>

                <nav className="flex-1 space-y-2">
                    <NavItem active={nav === 'dashboard'} icon={<LayoutDashboard size={20} />} label="Dashboard" onClick={() => setNav('dashboard')} />
                    <NavItem active={nav === 'add'} icon={<PlusCircle size={20} />} label="Add Reservation" onClick={() => setNav('add')} />
                    <NavItem active={nav === 'list'} icon={<List size={20} />} label="View All" onClick={() => setNav('list')} />
                    <NavItem active={nav === 'billing'} icon={<CreditCard size={20} />} label="Billing" onClick={() => setNav('billing')} />
                </nav>

                <div className="mt-auto pt-6 border-t border-slate-800">
                    <NavItem icon={<LogOut size={20} />} label="Logout" onClick={handleLogout} />
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 p-10 overflow-y-auto">
                <header className="mb-10 flex justify-between items-center">
                    <div>
                        <h2 className="text-3xl font-bold text-slate-900 capitalize">{nav}</h2>
                        <p className="text-slate-500">Welcome back to the management portal.</p>
                    </div>
                    <div className="flex items-center gap-4">
                        <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold">A</div>
                        <span className="font-medium">{localStorage.getItem('oceanview_username') || 'Administrator'}</span>
                    </div>
                </header>

                {nav === 'dashboard' && <Dashboard stats={stats} />}
                {nav === 'add' && <AddForm onCreated={() => { fetchData(); setNav('list'); }} />}
                {nav === 'list' && <ListView data={reservations} />}
                {nav === 'billing' && <BillingTerminal />}
            </main>
        </div>
    );
}

function LoginScreen({ onLogin }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        const success = onLogin(username, password);
        if (!success) {
            setError('Invalid username or password');
            setPassword('');
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-600 via-blue-700 to-slate-900 flex items-center justify-center p-6">
            <div className="w-full max-w-md">
                <div className="bg-white rounded-3xl shadow-2xl p-10 space-y-8">
                    <div className="text-center space-y-3">
                        <div className="w-16 h-16 bg-blue-600 rounded-2xl mx-auto flex items-center justify-center">
                            <Lock className="text-white" size={32} />
                        </div>
                        <h1 className="text-3xl font-black text-slate-900">OCEAN VIEW</h1>
                        <p className="text-slate-500 font-medium">Resort Management System</p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div className="space-y-2">
                            <label className="text-sm font-bold text-slate-700">Username</label>
                            <div className="relative">
                                <User className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                <input
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    className="w-full pl-11 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 transition-all"
                                    placeholder="Enter username"
                                    required
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-bold text-slate-700">Password</label>
                            <div className="relative">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                <input
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="w-full pl-11 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 transition-all"
                                    placeholder="Enter password"
                                    required
                                />
                            </div>
                        </div>

                        {error && (
                            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl text-sm font-medium">
                                {error}
                            </div>
                        )}

                        <button
                            type="submit"
                            className="w-full bg-blue-600 text-white py-4 rounded-xl font-bold hover:bg-blue-700 transition-all shadow-lg shadow-blue-600/30"
                        >
                            LOGIN
                        </button>
                    </form>

                    <div className="text-center pt-4 border-t border-slate-100">
                        <p className="text-xs text-slate-400 font-medium">
                            Demo credentials: <span className="text-blue-600 font-bold">admin / admin123</span>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}

function NavItem({ active, icon, label, onClick }) {
    return (
        <button
            onClick={onClick}
            className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${active ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20' : 'hover:bg-slate-800 hover:text-slate-200'
                }`}
        >
            {icon}
            <span className="font-medium">{label}</span>
        </button>
    );
}

function Dashboard({ stats }) {
    const chartData = [
        { name: 'Mon', count: 4 }, { name: 'Tue', count: 7 }, { name: 'Wed', count: 5 },
        { name: 'Thu', count: 12 }, { name: 'Fri', count: 9 }, { name: 'Sat', count: 15 }, { name: 'Sun', count: 10 }
    ];

    return (
        <div className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <StatCard label="Total Bookings" value={stats.totalBookings} color="blue" />
                <StatCard label="Revenue" value={`LKR ${stats.totalRevenue.toLocaleString()}`} color="green" />
                <StatCard label="Occupancy" value={stats.occupancyRate} color="amber" />
                <StatCard label="Active Guests" value="28" color="purple" />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                    <h3 className="font-bold text-lg mb-6 text-slate-800">Booking Trends</h3>
                    <div className="h-64 h-80">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#64748b' }} />
                                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#64748b' }} />
                                <Tooltip cursor={{ fill: '#f8fafc' }} contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }} />
                                <Bar dataKey="count" fill="#2563eb" radius={[4, 4, 0, 0]} barSize={40} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                    <h3 className="font-bold text-lg mb-6 text-slate-800">Quick Actions</h3>
                    <div className="space-y-4">
                        <button className="w-full text-left p-4 rounded-xl border border-slate-100 hover:border-blue-200 hover:bg-blue-50 transition-all group">
                            <span className="block font-bold">Print Daily Report</span>
                            <span className="text-sm text-slate-500">Summary of today's arrivals</span>
                        </button>
                        <button className="w-full text-left p-4 rounded-xl border border-slate-100 hover:border-blue-200 hover:bg-blue-50 transition-all">
                            <span className="block font-bold">Manage Staff</span>
                            <span className="text-sm text-slate-500">View active portal users</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

function StatCard({ label, value, color }) {
    const colors = {
        blue: 'bg-blue-50 text-blue-600',
        green: 'bg-emerald-50 text-emerald-600',
        amber: 'bg-amber-50 text-amber-600',
        purple: 'bg-indigo-50 text-indigo-600'
    };
    return (
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <span className="text-sm text-slate-500 font-medium">{label}</span>
            <div className={`mt-2 font-bold text-2xl`}>{value}</div>
        </div>
    );
}

function AddForm({ onCreated }) {
    const [formData, setFormData] = useState({
        guestName: '', address: '', phone: '', roomType: 'STANDARD', boardType: 'BB', checkInDate: '', checkOutDate: ''
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        const res = await fetch(API_BASE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        if (res.ok) {
            alert("Reservation Created Successfully!");
            onCreated();
        } else {
            const err = await res.json();
            const msg = err.message || (err.errors ? err.errors.map(e => e.defaultMessage).join(", ") : "Check your inputs");
            alert(`Error: ${msg}`);
        }
    };

    const calculateEstimate = () => {
        if (!formData.checkInDate || !formData.checkOutDate) return 0;
        const start = new Date(formData.checkInDate);
        const end = new Date(formData.checkOutDate);
        const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        if (nights <= 0) return 0;

        const roomRates = { STANDARD: 15000, DELUXE: 25000, SUITE: 45000 };
        const boardRates = { BB: 0, HB: 5000, FB: 10000 };

        return nights * (roomRates[formData.roomType] + boardRates[formData.boardType]);
    };

    const estimate = calculateEstimate();

    return (
        <div className="max-w-2xl bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-2 gap-6">
                    <Input label="Guest Name" type="text" value={formData.guestName} onChange={e => setFormData({ ...formData, guestName: e.target.value })} icon={<User size={18} />} />
                    <Input label="Phone" type="tel" value={formData.phone} onChange={e => setFormData({ ...formData, phone: e.target.value })} icon={<Phone size={18} />} />
                </div>
                <Input label="Address" type="text" value={formData.address} onChange={e => setFormData({ ...formData, address: e.target.value })} icon={<MapPin size={18} />} />

                <div className="grid grid-cols-2 gap-6">
                    <div className="space-y-2">
                        <label className="text-sm font-bold text-slate-700">Room Type</label>
                        <select
                            className="w-full p-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500"
                            value={formData.roomType}
                            onChange={e => setFormData({ ...formData, roomType: e.target.value })}
                        >
                            <option value="STANDARD">Standard (LKR 15,000)</option>
                            <option value="DELUXE">Deluxe (LKR 25,000)</option>
                            <option value="SUITE">Suite (LKR 45,000)</option>
                        </select>
                    </div>
                    <div className="space-y-2">
                        <label className="text-sm font-bold text-slate-700">Board Type</label>
                        <select
                            className="w-full p-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500"
                            value={formData.boardType}
                            onChange={e => setFormData({ ...formData, boardType: e.target.value })}
                        >
                            <option value="BB">Bed & Breakfast (Free)</option>
                            <option value="HB">Half Board (+5,000)</option>
                            <option value="FB">Full Board (+10,000)</option>
                        </select>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-6">
                    <Input label="Check In" type="date" value={formData.checkInDate} onChange={e => setFormData({ ...formData, checkInDate: e.target.value })} />
                    <Input label="Check Out" type="date" value={formData.checkOutDate} onChange={e => setFormData({ ...formData, checkOutDate: e.target.value })} />
                </div>

                {estimate > 0 && (
                    <div className="p-4 bg-blue-50 border border-blue-100 rounded-xl flex justify-between items-center">
                        <span className="text-blue-700 font-bold">Estimated Total Price:</span>
                        <span className="text-2xl font-black text-blue-900">LKR {estimate.toLocaleString()}</span>
                    </div>
                )}

                <button type="submit" className="w-full bg-blue-600 text-white py-4 rounded-xl font-bold hover:bg-blue-700 transition-all">
                    CONFIRM RESERVATION
                </button>
            </form>
        </div>
    );
}

function Input({ label, icon, ...props }) {
    return (
        <div className="space-y-2">
            <label className="text-sm font-bold text-slate-700">{label}</label>
            <div className="relative">
                {icon && <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">{icon}</div>}
                <input {...props} className={`w-full ${icon ? 'pl-11' : 'px-4'} py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 transition-all`} />
            </div>
        </div>
    );
}

function ListView({ data }) {
    return (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <table className="w-full text-left boreder-collapse">
                <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 text-sm font-bold">
                    <tr>
                        <th className="px-6 py-4">Reference</th>
                        <th className="px-6 py-4">Guest</th>
                        <th className="px-6 py-4">Plan</th>
                        <th className="px-6 py-4">Stay Dates</th>
                        <th className="px-6 py-4">Total Bill</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                    {data.map(r => (
                        <tr key={r.id} className="hover:bg-blue-50/50 transition-all underline-none">
                            <td className="px-6 py-4 font-bold text-blue-600">#{r.referenceId}</td>
                            <td className="px-6 py-4 font-medium">{r.guestName}</td>
                            <td className="px-6 py-4">
                                <div className="flex flex-col">
                                    <span className="text-sm font-bold">{r.roomType}</span>
                                    <span className="text-xs text-slate-400 font-medium">{r.boardType}</span>
                                </div>
                            </td>
                            <td className="px-6 py-4 text-slate-500 text-sm">{r.checkInDate} to {r.checkOutDate}</td>
                            <td className="px-6 py-4 font-bold">LKR {r.totalBill?.toLocaleString()}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

function BillingTerminal() {
    const [refId, setRefId] = useState('');
    const [bill, setBill] = useState(null);

    const findBill = async () => {
        const res = await fetch(`${API_BASE}/${refId}`);
        if (res.ok) setBill(await res.json());
        else alert("Reservation not found");
    };

    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
            <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6 self-start">
                <h3 className="font-bold text-lg">Billing Search</h3>
                <Input label="Enter Reference ID" value={refId} onChange={e => setRefId(e.target.value)} placeholder="e.g. 1001" />
                <button onClick={findBill} className="w-full bg-slate-900 text-white py-4 rounded-xl font-bold hover:bg-black transition-all">
                    GENERATE INVOICE
                </button>
            </div>

            {bill && (
                <div className="bg-white p-10 rounded-2xl border-4 border-slate-900 shadow-2xl space-y-6">
                    <div className="text-center border-b pb-6 space-y-2">
                        <h1 className="text-2xl font-black italic">OCEAN VIEW RESORT</h1>
                        <p className="text-xs uppercase tracking-widest text-slate-400 font-bold">Official Invoice</p>
                    </div>

                    <div className="grid grid-cols-2 gap-y-4 text-sm">
                        <span className="text-slate-400 font-bold">ID:</span> <span className="text-right font-bold text-blue-600">#{bill.referenceId}</span>
                        <span className="text-slate-400 font-bold">Guest:</span> <span className="text-right font-bold uppercase">{bill.guestName}</span>
                        <span className="text-slate-400 font-bold">Stay:</span> <span className="text-right font-bold">{bill.checkInDate} — {bill.checkOutDate}</span>
                        <span className="text-slate-400 font-bold">Room:</span> <span className="text-right font-bold underline decoration-blue-500 decoration-2">{bill.roomType}</span>
                    </div>

                    <div className="border-t-2 border-dashed border-slate-200 pt-6 flex justify-between items-center">
                        <span className="text-xl font-black uppercase tracking-tighter">Total Due</span>
                        <span className="text-3xl font-black text-slate-900">LKR {bill.totalBill?.toLocaleString()}</span>
                    </div>

                    <button onClick={() => window.print()} className="w-full mt-6 bg-slate-50 text-slate-400 py-3 rounded-lg text-sm font-bold border-2 border-slate-100 hover:bg-slate-100 transition-all uppercase tracking-widest">
                        Print Hard Copy
                    </button>
                </div>
            )}
        </div>
    );
}


function NavItem({ active, icon, label, onClick }) {
    return (
        <button
            onClick={onClick}
            className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${active ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20' : 'hover:bg-slate-800 hover:text-slate-200'
                }`}
        >
            {icon}
            <span className="font-medium">{label}</span>
        </button>
    );
}

function Dashboard({ stats }) {
    const chartData = [
        { name: 'Mon', count: 4 }, { name: 'Tue', count: 7 }, { name: 'Wed', count: 5 },
        { name: 'Thu', count: 12 }, { name: 'Fri', count: 9 }, { name: 'Sat', count: 15 }, { name: 'Sun', count: 10 }
    ];

    return (
        <div className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <StatCard label="Total Bookings" value={stats.totalBookings} color="blue" />
                <StatCard label="Revenue" value={`LKR ${stats.totalRevenue.toLocaleString()}`} color="green" />
                <StatCard label="Occupancy" value={stats.occupancyRate} color="amber" />
                <StatCard label="Active Guests" value="28" color="purple" />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                    <h3 className="font-bold text-lg mb-6 text-slate-800">Booking Trends</h3>
                    <div className="h-64 h-80">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#64748b' }} />
                                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#64748b' }} />
                                <Tooltip cursor={{ fill: '#f8fafc' }} contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }} />
                                <Bar dataKey="count" fill="#2563eb" radius={[4, 4, 0, 0]} barSize={40} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                    <h3 className="font-bold text-lg mb-6 text-slate-800">Quick Actions</h3>
                    <div className="space-y-4">
                        <button className="w-full text-left p-4 rounded-xl border border-slate-100 hover:border-blue-200 hover:bg-blue-50 transition-all group">
                            <span className="block font-bold">Print Daily Report</span>
                            <span className="text-sm text-slate-500">Summary of today's arrivals</span>
                        </button>
                        <button className="w-full text-left p-4 rounded-xl border border-slate-100 hover:border-blue-200 hover:bg-blue-50 transition-all">
                            <span className="block font-bold">Manage Staff</span>
                            <span className="text-sm text-slate-500">View active portal users</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

function StatCard({ label, value, color }) {
    const colors = {
        blue: 'bg-blue-50 text-blue-600',
        green: 'bg-emerald-50 text-emerald-600',
        amber: 'bg-amber-50 text-amber-600',
        purple: 'bg-indigo-50 text-indigo-600'
    };
    return (
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <span className="text-sm text-slate-500 font-medium">{label}</span>
            <div className={`mt-2 font-bold text-2xl`}>{value}</div>
        </div>
    );
}

function AddForm({ onCreated }) {
    const [formData, setFormData] = useState({
        guestName: '', address: '', phone: '', roomType: 'STANDARD', boardType: 'BB', checkInDate: '', checkOutDate: ''
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        const res = await fetch(API_BASE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        if (res.ok) {
            alert("Reservation Created Successfully!");
            onCreated();
        } else {
            const err = await res.json();
            const msg = err.message || (err.errors ? err.errors.map(e => e.defaultMessage).join(", ") : "Check your inputs");
            alert(`Error: ${msg}`);
        }
    };

    const calculateEstimate = () => {
        if (!formData.checkInDate || !formData.checkOutDate) return 0;
        const start = new Date(formData.checkInDate);
        const end = new Date(formData.checkOutDate);
        const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        if (nights <= 0) return 0;

        const roomRates = { STANDARD: 15000, DELUXE: 25000, SUITE: 45000 };
        const boardRates = { BB: 0, HB: 5000, FB: 10000 };

        return nights * (roomRates[formData.roomType] + boardRates[formData.boardType]);
    };

    const estimate = calculateEstimate();

    return (
        <div className="max-w-2xl bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-2 gap-6">
                    <Input label="Guest Name" type="text" value={formData.guestName} onChange={e => setFormData({ ...formData, guestName: e.target.value })} icon={<User size={18} />} />
                    <Input label="Phone" type="tel" value={formData.phone} onChange={e => setFormData({ ...formData, phone: e.target.value })} icon={<Phone size={18} />} />
                </div>
                <Input label="Address" type="text" value={formData.address} onChange={e => setFormData({ ...formData, address: e.target.value })} icon={<MapPin size={18} />} />

                <div className="grid grid-cols-2 gap-6">
                    <div className="space-y-2">
                        <label className="text-sm font-bold text-slate-700">Room Type</label>
                        <select
                            className="w-full p-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500"
                            value={formData.roomType}
                            onChange={e => setFormData({ ...formData, roomType: e.target.value })}
                        >
                            <option value="STANDARD">Standard (LKR 15,000)</option>
                            <option value="DELUXE">Deluxe (LKR 25,000)</option>
                            <option value="SUITE">Suite (LKR 45,000)</option>
                        </select>
                    </div>
                    <div className="space-y-2">
                        <label className="text-sm font-bold text-slate-700">Board Type</label>
                        <select
                            className="w-full p-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500"
                            value={formData.boardType}
                            onChange={e => setFormData({ ...formData, boardType: e.target.value })}
                        >
                            <option value="BB">Bed & Breakfast (Free)</option>
                            <option value="HB">Half Board (+5,000)</option>
                            <option value="FB">Full Board (+10,000)</option>
                        </select>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-6">
                    <Input label="Check In" type="date" value={formData.checkInDate} onChange={e => setFormData({ ...formData, checkInDate: e.target.value })} />
                    <Input label="Check Out" type="date" value={formData.checkOutDate} onChange={e => setFormData({ ...formData, checkOutDate: e.target.value })} />
                </div>

                {estimate > 0 && (
                    <div className="p-4 bg-blue-50 border border-blue-100 rounded-xl flex justify-between items-center">
                        <span className="text-blue-700 font-bold">Estimated Total Price:</span>
                        <span className="text-2xl font-black text-blue-900">LKR {estimate.toLocaleString()}</span>
                    </div>
                )}

                <button type="submit" className="w-full bg-blue-600 text-white py-4 rounded-xl font-bold hover:bg-blue-700 transition-all">
                    CONFIRM RESERVATION
                </button>
            </form>
        </div>
    );
}

function Input({ label, icon, ...props }) {
    return (
        <div className="space-y-2">
            <label className="text-sm font-bold text-slate-700">{label}</label>
            <div className="relative">
                {icon && <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">{icon}</div>}
                <input {...props} className={`w-full ${icon ? 'pl-11' : 'px-4'} py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 transition-all`} />
            </div>
        </div>
    );
}

function ListView({ data }) {
    return (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <table className="w-full text-left boreder-collapse">
                <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 text-sm font-bold">
                    <tr>
                        <th className="px-6 py-4">Reference</th>
                        <th className="px-6 py-4">Guest</th>
                        <th className="px-6 py-4">Plan</th>
                        <th className="px-6 py-4">Stay Dates</th>
                        <th className="px-6 py-4">Total Bill</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                    {data.map(r => (
                        <tr key={r.id} className="hover:bg-blue-50/50 transition-all underline-none">
                            <td className="px-6 py-4 font-bold text-blue-600">#{r.referenceId}</td>
                            <td className="px-6 py-4 font-medium">{r.guestName}</td>
                            <td className="px-6 py-4">
                                <div className="flex flex-col">
                                    <span className="text-sm font-bold">{r.roomType}</span>
                                    <span className="text-xs text-slate-400 font-medium">{r.boardType}</span>
                                </div>
                            </td>
                            <td className="px-6 py-4 text-slate-500 text-sm">{r.checkInDate} to {r.checkOutDate}</td>
                            <td className="px-6 py-4 font-bold">LKR {r.totalBill?.toLocaleString()}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

function BillingTerminal() {
    const [refId, setRefId] = useState('');
    const [bill, setBill] = useState(null);

    const findBill = async () => {
        const res = await fetch(`${API_BASE}/${refId}`);
        if (res.ok) setBill(await res.json());
        else alert("Reservation not found");
    };

    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
            <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6 self-start">
                <h3 className="font-bold text-lg">Billing Search</h3>
                <Input label="Enter Reference ID" value={refId} onChange={e => setRefId(e.target.value)} placeholder="e.g. 1001" />
                <button onClick={findBill} className="w-full bg-slate-900 text-white py-4 rounded-xl font-bold hover:bg-black transition-all">
                    GENERATE INVOICE
                </button>
            </div>

            {bill && (
                <div className="bg-white p-10 rounded-2xl border-4 border-slate-900 shadow-2xl space-y-6">
                    <div className="text-center border-b pb-6 space-y-2">
                        <h1 className="text-2xl font-black italic">OCEAN VIEW RESORT</h1>
                        <p className="text-xs uppercase tracking-widest text-slate-400 font-bold">Official Invoice</p>
                    </div>

                    <div className="grid grid-cols-2 gap-y-4 text-sm">
                        <span className="text-slate-400 font-bold">ID:</span> <span className="text-right font-bold text-blue-600">#{bill.referenceId}</span>
                        <span className="text-slate-400 font-bold">Guest:</span> <span className="text-right font-bold uppercase">{bill.guestName}</span>
                        <span className="text-slate-400 font-bold">Stay:</span> <span className="text-right font-bold">{bill.checkInDate} — {bill.checkOutDate}</span>
                        <span className="text-slate-400 font-bold">Room:</span> <span className="text-right font-bold underline decoration-blue-500 decoration-2">{bill.roomType}</span>
                    </div>

                    <div className="border-t-2 border-dashed border-slate-200 pt-6 flex justify-between items-center">
                        <span className="text-xl font-black uppercase tracking-tighter">Total Due</span>
                        <span className="text-3xl font-black text-slate-900">LKR {bill.totalBill?.toLocaleString()}</span>
                    </div>

                    <button onClick={() => window.print()} className="w-full mt-6 bg-slate-50 text-slate-400 py-3 rounded-lg text-sm font-bold border-2 border-slate-100 hover:bg-slate-100 transition-all uppercase tracking-widest">
                        Print Hard Copy
                    </button>
                </div>
            )}
        </div>
    );
}
