import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import PatientList from '../components/PatientList';

export default function LandingPage() {
	return (
		<div className='h-screen overflow-hidden bg-gray-50 grid grid-cols-1 md:grid-cols-[220px_1fr] lg:grid-cols-[256px_1fr]'>
			{/* Sidebar - Grid item spanning both rows */}
			<div className='row-span-2 h-screen'>
				<Sidebar
					mcrNo='M12345A'
					firstName='Jenny'
					clinicName='Raffles Medical Centre'
				/>
			</div>

			{/* Header - Second column, first row */}
			<div className='flex flex-col overflow-hidden'>
				<Header
					title='Assigned Patients'
					subtitle='Manage your current patient assignments'
				/>
			</div>

			{/* Main Content - Second column, second row */}
			<main className='flex-1 overflow-y-auto max-w-full p-4 sm:p-6 md:p-2'>
				<PatientList />
			</main>
		</div>
	);
}
