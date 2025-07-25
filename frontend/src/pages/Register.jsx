import React from 'react';
import { useNavigate } from 'react-router-dom';

function Register() {
	const navigate = useNavigate();
	// temporarily hard-coded, to eventually call GET API to extract list of clinics
	const clinics = ['Clinic A', 'Clinic B', 'Clinic C'];

	const handleSignIn = (e) => {
		e.preventDefault();
		navigate('/login', { replace: true });
	};

	return (
		<>
			<section>
				<div className='flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0'>
					<a
						href='#'
						className='flex items-center mb-6 text-2xl font-semibold text-gray-900'>
						<img
							className='w-8 h-8 mr-2'
							src='https://flowbite.s3.amazonaws.com/blocks/marketing-ui/logo.svg'
							alt='logo'
						/>
						MediMind
					</a>
					<div className='w-full max-w-2xl bg-white rounded-lg shadow-xl md:mt-0 sm:max-w-xl xl:p-0'>
						<div className='p-6 space-y-4 md:space-y-6 sm:p-8'>
							<h2 className='text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl'>
								Create an account
							</h2>
							<form
								className='space-y-4 md:space-y-6'
								action='#'>
								<div className='flex items-center justify-between gap-10'>
									<div>
										<label
											htmlFor='firstName'
											className='form-label'>
											First Name
										</label>
										<input
											type='text'
											name='firstName'
											id='firstName'
											className='form-input'
											placeholder='John'
											required></input>
									</div>
									<div>
										<label
											htmlFor='lastName'
											className='form-label'>
											Last Name
										</label>
										<input
											type='text'
											name='lastName'
											id='lastName'
											className='form-input'
											placeholder='Doe'
											required></input>
									</div>
								</div>
								<div>
									<label
										htmlFor='mcrNo'
										className='form-label'>
										MCR Number
									</label>
									<input
										type='text'
										name='mcrNo'
										id='mcrNo'
										className='form-input'
										placeholder='M1234567A'
										required
									/>
								</div>
								<div>
									<label
										htmlFor='email'
										className='form-label'>
										Email Address
									</label>
									<input
										type='email'
										name='email'
										id='email'
										className='form-input'
										placeholder='name@company.com'
										required
									/>
								</div>
								<div>
									<label
										htmlFor='clinic'
										className='form-label'>
										Practicing Clinic
									</label>
									<select
										name='clinic'
										id='clinic'
										className='form-input'
										required>
										<option
											value=''
											disabled
											selected>
											Select a clinic
										</option>
										{clinics.map((clinic) => (
											<option
												key={clinic}
												value={clinic}>
												{clinic}
											</option>
										))}
									</select>
								</div>

								<div className='flex items-center justify-between gap-10'>
									<div>
										<label
											htmlFor='password'
											className='form-label'>
											Password
										</label>
										<input
											type='password'
											name='password'
											id='password'
											placeholder='••••••••'
											className='form-input'
											required
										/>
									</div>
									<div>
										<label
											htmlFor='confirmPassword'
											className='form-label'>
											Confirm Password
										</label>
										<input
											type='password'
											name='confirmPassword'
											id='confirmPassword'
											placeholder='••••••••'
											className='form-input'
											required
										/>
									</div>
								</div>
								<button
									type='submit'
									className='btn-submit'>
									Sign Up
								</button>
								<p className='text-sm font-light text-gray-500 text-center'>
									Already have an account?{' '}
									<a
										href='#'
										className='link'
										onClick={(e) => handleSignIn(e)}>
										Sign in
									</a>
								</p>
							</form>
						</div>
					</div>
				</div>
			</section>
		</>
	);
}

export default Register;
