import { Squares2X2Icon, UserCircleIcon } from '@heroicons/react/20/solid';
import React from 'react';
import { Link } from 'react-router-dom';

function Sidebar({ mcrNo, firstName }) {
	return (
		<>
			<div className='flex h-screen m-0 p-0'>
				<aside className='w-64 bg-white shadow-xl text-gray-800 p-6 space-y-8'>
					<h2 className='text-2xl font-bold mb-8 text-center text-gray-700'>
						MediMind
					</h2>

					<hr className='border-t border-gray-300'></hr>
					<div>
						<h2 className='font-semibold text-center mb-10 mt-5'>
							Welcome, Dr. {firstName}
						</h2>
					</div>
					<nav className='flex flex-col space-y-4'>
						<Link
							className='nav-button'
							to={`/profile/${mcrNo}`}>
							<UserCircleIcon className='size-6 mr-2 text-gray-500' /> Account
						</Link>
						<Link
							className='nav-button'
							to='/'>
							<Squares2X2Icon className='size-6 mr-2 text-gray-500' /> Assigned
							Patients
						</Link>
					</nav>
				</aside>
			</div>
		</>
	);
}

export default Sidebar;
