import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import PatientList from '../components/PatientList';

export default function LandingPage() {
   return (
   	<div className="min-h-screen grid grid-cols-[256px_1fr] bg-gray-50">
   		{/* Sidebar - Grid item spanning both rows */}
   		<div className="row-span-2 h-screen">
   			<Sidebar
   				mcrNo='M12345A'
   				firstName='Jenny'
   				clinicName='Raffles Medical Centre'
   			/>
   		</div>
   		
   		{/* Header - Second column, first row */}
   		<div className="col-start-2 sticky top-0 z-10">
   			<Header
   				title='Assigned Patients'
   				subtitle='Manage your current patient assignments'
   			/>
   		</div>
   		
   		{/* Main Content - Second column, second row */}
   		<main className="col-start-2 overflow-y-auto">
   			<PatientList />
   		</main>
   	</div>
   );
}