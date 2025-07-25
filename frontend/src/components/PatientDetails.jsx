import React, { useState } from 'react';
import MedicationList from './MedicationList';

function PatientDetails({ patientId }) {
	const { patientInfo, setPatientInfo } = useState({});

	//call GET API to retrieve patient's information

	return (
		<>
			<div className='flex justify-between'>
				<div className='w-xs mr-30'>
					<label className='form-label'>Patient's Name</label>
					<input
						className='form-input'
						type='text'
						name='patientName'
						value='John Tan Ah Kow'
					/>
				</div>
				<div className='w-xs'>
					<label className='form-label'>Patient's NRIC</label>
					<input
						className='form-input'
						type='text'
						name='patientNRIC'
						value='S10234567A'
					/>
				</div>
			</div>
			<div className='mt-20'>
				<MedicationList />
			</div>
		</>
	);
}

export default PatientDetails;
