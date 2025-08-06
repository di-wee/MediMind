import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import MediMindContext from '../context/MediMindContext';

const AssignPatientPage = () => {
    const { doctorDetails } = useContext(MediMindContext);
    const [patients, setPatients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchUnassignedPatients = async () => {
            if (!doctorDetails?.mcr) {
                console.warn('doctorDetails.mcr is not ready yet');
                return;
            }

            console.log('Fetching unassigned patients for MCR:', doctorDetails.mcr);
            try {
                const response = await axios.get(`/api/patients/unassigned/${doctorDetails.mcr}`);
                console.log('Unassigned patients from backend:', response.data);
                setPatients(Array.isArray(response.data) ? response.data : []);
            } catch (err) {
                console.error('Error fetching unassigned patients:', err);
                setError('Failed to fetch patients');
            } finally {
                setLoading(false);
            }
        };

        fetchUnassignedPatients();
    }, [doctorDetails?.mcr]);

    const assignPatient = async (patientId) => {
        try {
            await axios.put(`/api/patients/assign`, {
                patientId: patientId,
                doctorId: doctorDetails.mcr,
            });
            setPatients((prev) => prev.filter((p) => p.id !== patientId));
        } catch (err) {
            console.error(err);
            alert('Failed to assign patient');
        }
    };

    if (!doctorDetails?.mcr || loading) {
        return <p className="p-6">Loading patients...</p>;
    }
    if (error) {
        return <p className="p-6 text-red-500">{error}</p>;
    }

    return (
        <div className="p-6">
            <h2 className="text-xl font-bold mb-4">Unassigned Patients in Your Clinic</h2>

            {patients.length === 0 ? (
                <p>No unassigned patients available.</p>
            ) : (
                <table className="min-w-full table-auto border">
                    <thead className="bg-gray-100">
                    <tr>
                        <th className="border px-4 py-2">Patient Name</th>
                        <th className="border px-4 py-2">NRIC</th>
                        <th className="border px-4 py-2">Email</th>
                        <th className="border px-4 py-2">Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {patients.map((p) => (
                        <tr key={p.id}>
                            <td className="border px-4 py-2">
                                {p.firstName} {p.lastName}
                            </td>
                            <td className="border px-4 py-2">{p.nric}</td>
                            <td className="border px-4 py-2">{p.email}</td>
                            <td className="border px-4 py-2 text-center">
                                <button
                                    onClick={() => assignPatient(p.id)}
                                    className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700"
                                >
                                    Assign to Me
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default AssignPatientPage;
