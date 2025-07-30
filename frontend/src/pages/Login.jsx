import { XCircleIcon } from '@heroicons/react/16/solid';
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/20/solid';
import React, { useContext, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import MediMindContext from '../context/MediMindContext';

function Login() {
	const [validation, setValidation] = useState(false);
	const [passVisibility, setPassVisibility] = useState(false);
	const mmCtxt = useContext(MediMindContext);
	const { setDoctorInfo } = mmCtxt;

	const navigate = useNavigate();

	const mcrNoRef = useRef();
	const passwordRef = useRef();

	const mockData = {
		firstName: 'Jenny',
		lastName: 'Goh',
		mcrNo: 'M12345A',
		email: 'jenny.g@gmail.com',
		clinic: 'Raffles Medical Centre',
	};

	const handleSignUp = (e) => {
		e.preventDefault();
		navigate('/register', { replace: true });
	};

	const handleLogin = (e) => {
		e.preventDefault();
		const mcrNo = mcrNoRef.current.value;
		const password = passwordRef.current.value;

		//temp logic if both fields are not null will simulate authentication
		if (mcrNo && password) {
			setValidation(false);
			//setting key = 'isLoggedIn' with a string value = 'true'
			localStorage.setItem('isLoggedIn', 'true');
			setDoctorInfo(mockData);
			navigate('/', { replace: true });
		} else {
			setValidation(true);
		}
	};

	return (
		<>
			<section>
				<div className='login-container'>
					<h2 className='flex items-center text-2xl font-semibold text-gray-800 '>
						MediMind
					</h2>
					<div className='w-96 bg-white rounded-lg shadow-xl md:mt-0 sm:max-w-md xl:p-0'>
						<div className='p-6 space-y-4 md:space-y-6 sm:p-8'>
							<h2 className='text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl'>
								Welcome back!
							</h2>
							{validation && (
								<div className='login-validation relative'>
									<p>Invalid username or password!</p>
									<XCircleIcon
										className='size-4.5 absolute right-5 top-5 cursor-pointer'
										onClick={() => setValidation(false)}
									/>
								</div>
							)}

							<form
								className='space-y-4 md:space-y-6'
								action='#'>
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
										className='form-input '
										placeholder='M1234567A'
										ref={mcrNoRef}
										required
									/>
								</div>
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
											ref={passwordRef}
											required
										/>
										{passVisibility ? (
											<EyeSlashIcon
												onClick={() => setPassVisibility(false)}
												className='size-5 relative left-70 bottom-8 cursor-pointer'
											/>
										) : (
											<EyeIcon
												onClick={() => setPassVisibility(true)}
												className='size-5 relative left-70 bottom-8 cursor-pointer'
											/>
										)}
									</div>
								</div>

								<button
									type='submit'
									className='btn-submit'
									onClick={(e) => handleLogin(e)}>
									Sign in
								</button>
								<p className='text-sm font-light text-gray-500 text-center'>
									Don’t have an account yet?{' '}
									<a
										href='#'
										className='link'
										onClick={(e) => handleSignUp(e)}>
										Sign up
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

export default Login;
