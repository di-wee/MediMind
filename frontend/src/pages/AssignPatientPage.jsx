import React, { useEffect, useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import MediMindContext from '../context/MediMindContext';

const AssignPatientPage = () => {
    const { doctorDetails } = useContext(MediMindContext);
    const [patients, setPatients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    // Fallback for MCR from localStorage if not in context
    let mcr = doctorDetails?.mcr;
    if (!mcr) {
        const stored = localStorage.getItem('doctorDetails');
        if (stored) {
            try {
                mcr = JSON.parse(stored).mcr;
            } catch {
                mcr = null;
            }
        }
    }

    // Fetch unassigned patients for the doctor
    useEffect(() => {
        const fetchUnassignedPatients = async () => {
            if (!mcr) {
                setError('Doctor MCR not available');
                setLoading(false);
                return;
            }
            try {
                const response = await axios.get(`/api/patients/unassigned/${mcr}`);
                setPatients(Array.isArray(response.data) ? response.data : []);
            } catch {
                setError('Failed to fetch unassigned patients');
            } finally {
                setLoading(false);
            }
        };
        fetchUnassignedPatients();
    }, [mcr]);

    // Handle assigning patient to doctor
    const handleAssign = async (patientId) => {
        try {
            await axios.put('/api/patients/assign', {
                patientId,
                doctorId: mcr,
            });
            // Remove assigned patient from list instantly
            setPatients((prev) => prev.filter((p) => p.id !== patientId));
        } catch {
            alert('Assignment failed!');
        }
    };

    if (loading) return <div>Loading unassigned patients...</div>;
    if (error) return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div style={{ maxWidth: '900px', margin: '0 auto' }}>
            <button
                onClick={() => navigate(-1)}
                style={{
                    margin: '16px 0',
                    padding: '8px 16px',
                    background: '#334155',
                    color: 'white',
                    border: 'none',
                    borderRadius: '6px',
                    cursor: 'pointer'
                }}
            >
                &larr; Back
            </button>
            <h2>Unassigned Patients</h2>
            {patients.length === 0 ? (
                <div>No unassigned patients found.</div>
            ) : (
                <table style={{ borderCollapse: 'collapse', width: '100%', marginTop: '16px' }}>
                    <thead>
                    <tr>
                        <th style={{ borderBottom: '1px solid #ddd', padding: '8px' }}>Name</th>
                        <th style={{ borderBottom: '1px solid #ddd', padding: '8px' }}>Email</th>
                        <th style={{ borderBottom: '1px solid #ddd', padding: '8px' }}>NRIC</th>
                        <th style={{ borderBottom: '1px solid #ddd', padding: '8px' }}>Clinic</th>
                        <th style={{ borderBottom: '1px solid #ddd', padding: '8px' }}>DOB</th>
                        <th style={{ borderBottom: '1px solid #ddd', padding: '8px' }}>Assign</th>
                    </tr>
                    </thead>
                    <tbody>
                    {patients.map((patient) => (
                        <tr key={patient.id}>
                            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>
                                <strong>{patient.firstName} {patient.lastName}</strong>
                            </td>
                            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>
                                {patient.email}
                            </td>
                            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>
                                {patient.nric}
                            </td>
                            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>
                                {patient.clinic?.clinicName}
                            </td>
                            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>
                                {patient.dob}
                            </td>
                            <td style={{ borderBottom: '1px solid #eee', padding: '8px' }}>
                                <button
                                    onClick={() => handleAssign(patient.id)}
                                    style={{
                                        padding: '6px 12px',
                                        background: '#059669',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '4px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    Assign
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
