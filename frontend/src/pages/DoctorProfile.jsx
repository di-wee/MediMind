import React from 'react';
import { useParams } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import DoctorDetails from '../components/DoctorDetails';

function DoctorProfile() {
	//this will extract the doctor's MCRNo from the endpoint /profile/:mcrNo
	const { mcrNo } = useParams();

	// Store patients assigned to this doctor
	const [patients, setPatients] = useState([]);

	// Fetch patients from backend
	const fetchPatients = async () => {
		try {
			const response = await axios.get(`http://localhost:8080/api/patients/by-doctor/${mcrNo}`);
			setPatients(response.data);
		} catch (error) {
			console.error("Error fetching patients:", error);
		}
	};

	// Unassign doctor from patient
	const unassignDoctor = async (patientId) => {
		try {
			await axios.put(`http://localhost:8080/api/patients/${patientId}/unassign-doctor`);
			fetchPatients(); // Refresh list after unassigning
		} catch (error) {
			console.error("Failed to unassign doctor:", error);
		}
	};

	// Run fetch when component mounts or when mcrNo changes
	useEffect(() => {
		fetchPatients();
	}, [mcrNo]);

	return (
		<>
			<div className='h-screen overflow-hidden bg-gray-50 grid grid-cols-1 md:grid-cols-[220px_1fr] lg:grid-cols-[256px_1fr]'>
				<div className='row-span-full'>
					<Sidebar />
				</div>
				<div className='flex flex-col overflow-hidden'>
					<Header
						title='Account'
						subtitle='View your profile details'
					/>
					<div className='flex-1 overflow-y-auto max-w-full p-4 sm:p-6 md:p-2'>
						<div className='overflow-x-auto'>
							<DoctorDetails mcrNo={mcrNo} />
						</div>

						{/* Display assigned patients */}
						<div className="mt-6">
							<h2 className="text-lg font-semibold mb-2">Assigned Patients:</h2>
							{patients.length === 0 ? (
								<p>No patients assigned.</p>
							) : (
								<ul className="space-y-2">
									{patients.map((patient) => (
										<li
											key={patient.id}
											className="flex justify-between items-center bg-white p-4 rounded shadow"
										>
											<span>{patient.firstName} {patient.lastName}</span>
											<button
												onClick={() => unassignDoctor(patient.id)}
												className="px-4 py-1 bg-red-500 hover:bg-red-600 text-white rounded"
											>
												Unassign
											</button>
										</li>
									))}
								</ul>
							)}
						</div>
					</div>
				</div>
			</div>
		</>
	);
}

export default DoctorProfile;
