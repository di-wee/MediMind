import React from 'react';

function DoctorDetails() {
	//sidebar and header is taken care off, just need you to do the UI layout for the
	//doctor's profile. can refer to layout design for in the PatientDetails component
	//no need display pic
	//
	//so far i've only copy pasted the html code over from the PatientDetails component to
	//help get u started. jiayous!

	return (
		<>
			<main className='w-full flex-1 mt-20 bg-gray-50 min-h-screen'>
				{/* shadow-xl bg-white py-8 m-5 rounded-xl this is giving the shadow box effect */}
				<div className='shadow-xl bg-white py-8 m-5 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Account Details</h2>
					<div className='patient-details'>
						<div className='w-xl'>
							<label className='form-label'>Patient's Name</label>
							<input
								className='form-input'
								type='text'
								name='patientName'
								value=''
								readOnly
							/>
						</div>
						<div className='w-xl '>
							<label className='form-label'>Patient's NRIC</label>
							<input
								className='form-input'
								type='text'
								name='patientNRIC'
								value=''
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
								value=''
								readOnly
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>Patient's Age</label>
							<input
								className='form-input'
								type='text'
								name='patientAge'
								value=''
								readOnly
							/>
						</div>
					</div>
				</div>
			</main>
		</>
	);
}

export default DoctorDetails;
