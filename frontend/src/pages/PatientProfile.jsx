import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import PatientDetails from '../components/PatientDetails';
function PatientProfile() {
	const { patientId } = useParams();

	return (
		<>
			<div className='h-screen overflow-hidden bg-gray-50 grid grid-cols-1 md:grid-cols-[220px_1fr] lg:grid-cols-[256px_1fr]'>
				<div>
					<Sidebar />
				</div>

				<div className='flex flex-col overflow-hidden'>
					<Header
						title='Assigned Patients'
						subtitle='Patient Profile'
					/>

					<div className='flex-1 overflow-y-auto max-w-full p-4 sm:p-6 md:p-2'>
						<div className='overflow-x-auto overflow-y-hidden'>
							<PatientDetails patientId={patientId} />
						</div>
					</div>
				</div>
			</div>
		</>
	);
}

export default PatientProfile;
