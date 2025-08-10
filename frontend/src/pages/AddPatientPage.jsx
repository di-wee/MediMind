import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import { useContext, useEffect, useState } from 'react';
import MediMindContext from '../context/MediMindContext';
import UnassignPatientList from '../components/UnassignPatientList';
import { API_BASE_URL } from '../utils/config';

export default function AddPatientPage() {
	const [searchTerm, setSearchTerm] = useState('');
	const [displayList, setDisplayList] = useState([]);
	const [unassignedPatients, setUnassignedPatients] = useState([]);
	const mediMindCtx = useContext(MediMindContext);
	const { doctorDetails } = mediMindCtx;

	const handleAssign = async (patientId) => {
		console.log(`Assigning patient with ID: ${patientId}`);

		try {
			const response = await fetch(API_BASE_URL + 'api/patients/assign', {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
				},
				body: JSON.stringify({
					patientId,
					doctorId: doctorDetails.mcrNo,
				}),
			});

			if (response.ok) {
				//to update state to get the component to refresh
				setUnassignedPatients((prev) => prev.filter((p) => p.id !== patientId));
				setDisplayList((prev) => prev.filter((p) => p.id !== patientId));
			}
		} catch (err) {
			console.error('Error in assigning patient to Doctor: ', err);
		}
	};

	useEffect(() => {
		const fetchUnassignedPatients = async () => {
			try {
				const response = await fetch(
					API_BASE_URL + `api/patients/unassigned/${doctorDetails.mcrNo}`,
					{
						method: 'GET',
						headers: {
							'Content-Type': 'application/json',
						},
					}
				);

				if (response.ok) {
					const patients = await response.json();
					setDisplayList(patients);
					setUnassignedPatients(patients);
				}
			} catch (err) {
				console.error('Error fetching unassigned patients: ', err);
			}
		};
		fetchUnassignedPatients();
	}, [doctorDetails]);

	useEffect(() => {
		const filteredPatients = unassignedPatients.filter(
			(patient) =>
				patient.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
				patient.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
				patient.nric.toLowerCase().includes(searchTerm.toLowerCase())
		);
		setDisplayList(filteredPatients);
	}, [searchTerm]);

	return (
		<div className='h-screen overflow-hidden bg-gray-50 grid grid-cols-1 md:grid-cols-[220px_1fr] lg:grid-cols-[256px_1fr]'>
			<div className='row-span-2 h-screen'>
				<Sidebar />
			</div>

			{/* Header - Second column, first row */}
			<div className='flex flex-col overflow-hidden'>
				<Header
					title='Add New Patient'
					subtitle='Assign unassigned patients to your care'
				/>
			</div>
			<main className='flex-1 overflow-y-auto max-w-full p-4 sm:p-6 md:p-2'>
				<UnassignPatientList
					setSearchTerm={setSearchTerm}
					searchTerm={searchTerm}
					displayList={displayList}
					unassignedPatients={unassignedPatients}
					handleAssign={handleAssign}
				/>
			</main>
		</div>
	);
}
