import React, { useState } from 'react';
import MedicationList from './MedicationList';

function PatientDetails({ patientId }) {
	const { patientInfo, setPatientInfo } = useState({});

	//call GET API to retrieve patient's information

	return (
		<>
			<div className='patient-details'>
				<div className='w-1/2'>
					<label className='form-label'>Patient's Name</label>
					<input
						className='form-input'
						type='text'
						name='patientName'
						value='John Tan Ah Kow'
						readOnly
					/>
				</div>
				<div className='w-1/2 '>
					<label className='form-label'>Patient's NRIC</label>
					<input
						className='form-input'
						type='text'
						name='patientNRIC'
						value='S10234567A'
						readOnly
					/>
				</div>
			</div>
			<div className='mt-20'>
				<MedicationList patientId={patientId} />
			</div>
		</>
	);
}

export default PatientDetails;
