import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import { useState } from 'react';
import {
	PlusIcon,
	MagnifyingGlassIcon,
	UserPlusIcon,
} from '@heroicons/react/24/outline';
import allPatients from '../mockdata/patientlist.json';
import PatientGrid from '../components/PatientGrid';
import PatientList from '../components/PatientList';

export default function LandingPage() {
	return (
		<>
			<div className='grid-cols-4 h-screen'>
				{/* Sidebar */}
				<div className='row-span-full'>
					<Sidebar
						mcrNo='M12345A'
						firstName='Jenny'
						clinicName='Raffles Medical Centre'
					/>
				</div>
				<div className='col-span-full ml-64 h-20'>
					<Header
						title='Assigned Patients'
						subtitle='Manage your current patient assignments'
					/>
				</div>
				<div className='flex-1 overflow-y-auto px-6 mt-20 justify-items-center'>
					<PatientList />
				</div>
			</div>
		</>
	);
}
