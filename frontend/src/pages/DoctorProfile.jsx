import React from 'react';
import { useParams } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import PageHeader from '../components/Header';
import DoctorDetails from '../components/DoctorDetails';

function DoctorProfile() {
	//this will extract the doctor's MCRNo from the endpoint /profile/:mcrNo
	const { mcrNo } = useParams();

	return (
		<>
			<div className='grid-cols-4 h-screen'>
				<div className='row-span-full'>
					<Sidebar />
				</div>
				<div className='col-span-full ml-64'>
					<Header
						title='Account'
						subtitle='View your profile details'
					/>
					<div className='flex overflow-y-auto mt-10 justify-items-center'>
						<DoctorDetails mcrNo={mcrNo} />
					</div>
				</div>
			</div>
		</>
	);
}

export default DoctorProfile;
