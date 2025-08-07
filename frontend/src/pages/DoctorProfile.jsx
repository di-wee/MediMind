import React from 'react';
import { useParams } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import DoctorDetails from '../components/DoctorDetails';

function DoctorProfile() {
	//this will extract the doctor's MCRNo from the endpoint /profile/:mcrNo
	const { mcrNo } = useParams();

	return (
		<>
			<div className='h-screen overflow-hidden bg-gray-50 grid grid-cols-1 md:grid-cols-[220px_1fr] lg:grid-cols-[256px_1fr]'>
				<div className='row-span-full'>
					<Sidebar />
				</div>
				<div className='flex flex-col overflow-hidden'>
					<Header
						title='Account'
						subtitle='View your profile details'
					/>
					<div className='flex-1 overflow-y-auto max-w-full p-4 sm:p-6 md:p-2'>
						<div className='overflow-x-auto'>
							<DoctorDetails mcrNo={mcrNo} />
						</div>
					</div>
				</div>
			</div>
		</>
	);
}

export default DoctorProfile;
