import React from 'react';
import { useParams } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import PatientDetails from '../components/PatientDetails';
function PatientProfile() {
	const { patientId } = useParams();
	return (
		<>
			<div className='flex h-screen'>
				<Sidebar
					mcrNo='M12345A'
					firstName='Jennifer'
				/>

				<div className='ml-64 h-20'>
					<Header />

					<div className='flex-1 overflow-y-auto px-6 mt-20 justify-items-center'>
						<PatientDetails patientId={patientId} />
					</div>
				</div>
			</div>
		</>
	);
}

export default PatientProfile;
