import React from 'react';

function DoctorDetails({ mcrNo }) {
	//1. need you to help with the update details mock functionality.
	//so on clicking update details, it will only allow
	//email address and practicing clinic to be updated.
	//and when u click the update button, it will change to a save button where on click it will
	//save the changes made
	//
	//2. this one not so important, but on button click of change password, it will come up a second
	//input field  [confirm password]  next to the password field
	// change password button will change to save password. on button click, it will save the changed password
	//
	//u will need to useState to manage all of this. jiayous! most important to finish feature 1, feature2 cannot nvm.
	return (
		<>
			<main className='w-full flex-1 mt-23  bg-gray-50 min-h-screen'>
				{/* shadow-xl bg-white py-8 m-5 rounded-xl this is giving the shadow box effect */}

				<div className='border-1 border-gray-200 shadow-xl bg-white pt-8 m-5 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Account Details</h2>
					<div className='profile-details'>
						<div className='w-xl'>
							<label className='form-label'>First Name</label>
							<input
								className='form-input'
								type='text'
								name='firstName'
								value='Jenny'
								readOnly
							/>
						</div>
						<div className='w-xl '>
							<label className='form-label'>Last Name</label>
							<input
								className='form-input'
								type='text'
								name='lastName'
								value='Goh'
								readOnly
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>MCR No</label>
							<input
								className='form-input'
								type='text'
								name='MCRNo'
								value='M12345A'
								readOnly
							/>
						</div>
					</div>
					<div className='profile-details pt-5 mb-15'>
						<div className='w-xl'>
							<label className='form-label'>Email Address</label>
							<input
								className='form-input'
								type='text'
								name='email'
								value='jenny.g@gmail.com'
								readOnly
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>Practicing Clinic</label>
							<input
								className='form-input'
								type='text'
								name='firstName'
								value='Raffles Medical Centre'
								readOnly
							/>
						</div>
					</div>
				</div>
				<div className='border-1 border-gray-200 shadow-xl bg-white py-8 m-5 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Password</h2>
					<div className='password-details'>
						<div className='w-1/2'>
							<label className='form-label'>Password</label>
							<input
								className='form-input'
								type='password'
								name='firstName'
								value='medimind123'
								readOnly
							/>
						</div>
						<div className='flex items-end'>
							<button className='btn-submit'>Change Password</button>
						</div>
					</div>
				</div>
				<div className='flex justify-end px-5 py-5'>
					<button className='btn-submit max-w-40 end'>Update Details</button>
				</div>
			</main>
		</>
	);
}

export default DoctorDetails;
