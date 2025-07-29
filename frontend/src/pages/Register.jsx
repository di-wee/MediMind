import { EyeIcon, EyeSlashIcon } from '@heroicons/react/20/solid';
import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Register() {
	const navigate = useNavigate();
	const [validation, setValidation] = useState({
		MCRNo: false,
		ConfirmPass: false,
	});
	const [clinicList, setClinicList] = useState([]);
	const [passVisibility, setPassVisibility] = useState(false);
	const [confirmPassVisibility, setConfirmPassVisibility] = useState(false);
	const [selectedClinic, setSelectedClinic] = useState('');

	const mcrRef = useRef();
	const firstNameRef = useRef();
	const lastNameRef = useRef();
	const emailRef = useRef();
	const passwordRef = useRef();
	const confirmPassRef = useRef();

	// temporarily hard-coded, to eventually call GET API to extract list of clinics
	const clinics = ['Clinic A', 'Clinic B', 'Clinic C'];

	const handleSignIn = (e) => {
		e.preventDefault();
		navigate('/login', { replace: true });
	};

	const handleSignUp = (e) => {
		e.preventDefault();

		const form = e.target;

		//letting the inbuilt validation run first
		if (!form.checkValidity()) {
			form.reportValidity();
			return;
		}

		const mcr = mcrRef.current.value.trim();
		const firstName = firstNameRef.current.value.trim();
		const lastName = lastNameRef.current.value.trim();
		const email = emailRef.current.value.trim();
		const password = passwordRef.current.value;
		const confirmPass = confirmPassRef.current.value;

		const errors = {
			MCRNo: mcr.length !== 7,
			ConfirmPass: password !== confirmPass,
		};

		setValidation(errors);

		//setting bool flag if any of the values in the KV of errors is true (means got error)
		const hasErrors = Object.values(errors).some((val) => val == true);
		if (hasErrors) return; // stopping logic if validation triggered

		//just simulating flow, proper logic will come in here eg. api calls
		//setting key ='isLoggedIn' with a string value 'true' (not boolean)
		localStorage.setItem('isLoggedIn', 'true');
		navigate('/', { replace: true });
	};

	const handleOptionOnChange = () => {};

	useEffect(() => {
		//get API to be called here to retrieve clinic list to be mapped
		setClinicList(clinics);
	}, []);

	return (
		<>
			<section>
				<div className='flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0 mt-20 '>
					<h2 className='flex items-center  text-2xl font-semibold text-gray-800'>
						MediMind
					</h2>
					<div className='w-full max-w-2xl bg-white rounded-lg shadow-xl md:mt-0 sm:max-w-xl xl:p-0 mb-20'>
						<div className='p-6 space-y-4 md:space-y-6 sm:p-8'>
							<h2 className='text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl'>
								Create an account
							</h2>
							<form
								className='space-y-4 md:space-y-6'
								action='#'
								onSubmit={(e) => handleSignUp(e)}>
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
											ref={firstNameRef}
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
											ref={lastNameRef}
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
										placeholder='M12345A'
										ref={mcrRef}
										required
									/>
									{validation['MCRNo'] && (
										<p className='inline-val-msg'>
											Please enter a valid MCR Number.
										</p>
									)}
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
										ref={emailRef}
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
										{clinicList.map((clinic) => (
											<option
												key={clinic}
												value={clinic}
												onChange={() => handleOptionOnChange()}>
												{clinic}
											</option>
										))}
									</select>
								</div>

								<div className='flex justify-between gap-10'>
									<div>
										<label
											htmlFor='password'
											className='form-label'>
											Password
										</label>
										<div>
											<input
												type={passVisibility ? 'text' : 'password'}
												name='password'
												id='password'
												placeholder='••••••••'
												className='form-input'
												minLength='8'
												ref={passwordRef}
												required
											/>
											{passVisibility ? (
												<EyeSlashIcon
													onClick={() => setPassVisibility(false)}
													className='size-5 relative left-40 bottom-8 cursor-pointer'
												/>
											) : (
												<EyeIcon
													onClick={() => setPassVisibility(true)}
													className='size-5 relative left-40 bottom-8 cursor-pointer'
												/>
											)}
										</div>
									</div>
									<div>
										<label
											htmlFor='confirmPassword'
											className='form-label'>
											Confirm Password
										</label>
										<div>
											<input
												type={confirmPassVisibility ? 'text' : 'password'}
												name='confirmPassword'
												id='confirmPassword'
												placeholder='••••••••'
												className='form-input'
												ref={confirmPassRef}
												required
											/>
											{confirmPassVisibility ? (
												<EyeSlashIcon
													onClick={() => setConfirmPassVisibility(false)}
													className='size-5 relative left-40 bottom-8 cursor-pointer'
												/>
											) : (
												<EyeIcon
													onClick={() => setConfirmPassVisibility(true)}
													className='size-5 relative left-40 bottom-8 cursor-pointer'
												/>
											)}

											{validation['ConfirmPass'] && (
												<p className='inline-val-msg'>
													Password does not match.
												</p>
											)}
										</div>
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
