import React, { useEffect, useState } from 'react';
import MedicationList from './MedicationList';
import patientList from '../mockdata/patientlist.json';

function PatientDetails({ patientId }) {
	const [patientInfo, setPatientInfo] = useState({});

	//call GET API to retrieve patient's information
	useEffect(() => {
		const patient = patientList.find((p) => p.id === parseInt(patientId));
		console.log(patient);

		// Compute derived fields
		const fullName = `${patient.firstName} ${patient.lastName}`;
		const dob = new Date(patient.dob);
		const today = new Date();

		let age = today.getFullYear() - dob.getFullYear();
		if (
			today.getMonth() < dob.getMonth() ||
			(today.getMonth() === dob.getMonth() && today.getDate() < dob.getDate())
		) {
			age--;
		}

		setPatientInfo({
			...patient,
			fullName,
			age,
		});
	}, [patientId]);

	return (
		<>
			<main className='w-full flex-1 mt-20 bg-gray-50 min-h-screen'>
				<div className='shadow-xl bg-white py-8 m-5 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Patient Details</h2>
					<div className='patient-details'>
						<div className='w-xl'>
							<label className='form-label'>Patient's Name</label>
							<input
								className='form-input'
								type='text'
								name='patientName'
								value={patientInfo.fullName}
								readOnly
							/>
						</div>
						<div className='w-xl '>
							<label className='form-label'>Patient's NRIC</label>
							<input
								className='form-input'
								type='text'
								name='patientNRIC'
								value={patientInfo.nric}
								readOnly
							/>
						</div>
					</div>
					<div className='patient-details mb-5'>
						<div className='w-xl'>
							<label className='form-label'>Patient's DOB</label>
							<input
								className='form-input'
								type='text'
								name='patientDob'
								value={patientInfo.dob}
								readOnly
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>Patient's Age</label>
							<input
								className='form-input'
								type='text'
								name='patientAge'
								value={patientInfo.age}
								readOnly
							/>
						</div>
					</div>
				</div>
				<div className='mt-8'>
					<MedicationList patientId={patientId} />
				</div>
			</main>
		</>
	);
}

export default PatientDetails;
