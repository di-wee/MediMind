import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import { useState } from 'react';
import { PlusIcon, MagnifyingGlassIcon, UserPlusIcon } from '@heroicons/react/24/outline';
import PatientGrid from '../components/PatientGrid';

export default function LandingPage() {
  const allPatients = [
    { id: 1, firstName: "Tan", lastName: "Wei Ming", nric: "S1234567A", gender: "Male", dob: "15 Jan 1960", clinicName: "Raffles Medical Centre", assignedDoctor: "Dr. Jenny Goh" },
    { id: 2, firstName: "Lim", lastName: "Jia Hui", nric: "S2345678B", gender: "Female", dob: "22 Mar 1967", clinicName: "Mount Elizabeth Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 3, firstName: "Nur", lastName: "Aisyah", nric: "S3456789C", gender: "Female", dob: "08 Jul 1983", clinicName: "Singapore General Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 4, firstName: "Rajesh", lastName: "Kumar", nric: "S4567890D", gender: "Male", dob: "12 Nov 1954", clinicName: "Tan Tock Seng Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 5, firstName: "Goh", lastName: "Li Fang", nric: "S5678901E", gender: "Female", dob: "03 May 1966", clinicName: "National Heart Centre", assignedDoctor: "Dr. Jenny Goh" },
    { id: 6, firstName: "Mohamed", lastName: "Irfan", nric: "S6789012F", gender: "Male", dob: "28 Sep 1981", clinicName: "Changi General Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 7, firstName: "Chong", lastName: "Mei Ying", nric: "S7890123G", gender: "Female", dob: "17 Dec 1972", clinicName: "KK Women's and Children's Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 8, firstName: "Pravin", lastName: "Nair", nric: "S8901234H", gender: "Male", dob: "04 Feb 1977", clinicName: "Alexandra Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 9, firstName: "Lee", lastName: "Jun Hao", nric: "S9012345I", gender: "Male", dob: "11 Aug 1990", clinicName: "Jurong Community Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 10, firstName: "Farah", lastName: "Binte Osman", nric: "S0123456J", gender: "Female", dob: "25 Jun 1986", clinicName: "Institute of Mental Health", assignedDoctor: "Dr. Jenny Goh" },
    { id: 11, firstName: "Tan", lastName: "Kai Ling", nric: "S1234567K", gender: "Male", dob: "19 Oct 1963", clinicName: "National University Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 12, firstName: "Ong", lastName: "Zi Xuan", nric: "S2345678L", gender: "Female", dob: "07 Apr 1969", clinicName: "Gleneagles Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 13, firstName: "Siti", lastName: "Nurhaliza", nric: "S3456789M", gender: "Female", dob: "14 Jan 1984", clinicName: "Parkway East Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 14, firstName: "Vinod", lastName: "Ramesh", nric: "S4567890N", gender: "Male", dob: "30 Sep 1957", clinicName: "Ng Teng Fong General Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 15, firstName: "Chua", lastName: "Hui Wen", nric: "S5678901O", gender: "Female", dob: "23 Nov 1964", clinicName: "Sengkang General Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 16, firstName: "Mohamed", lastName: "Faizal", nric: "S6789012P", gender: "Male", dob: "16 May 1978", clinicName: "Khoo Teck Puat Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 17, firstName: "Lim", lastName: "Yi Xin", nric: "S7890123Q", gender: "Female", dob: "09 Aug 1973", clinicName: "Farrer Park Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 18, firstName: "Anil", lastName: "Kumar", nric: "S8901234R", gender: "Male", dob: "26 Dec 1976", clinicName: "Mount Alvernia Hospital", assignedDoctor: "Dr. Jenny Goh" },
    { id: 19, firstName: "Tan", lastName: "Yu Heng", nric: "S9012345S", gender: "Male", dob: "13 Mar 1988", clinicName: "Thomson Medical Centre", assignedDoctor: "Dr. Jenny Goh" },
    { id: 20, firstName: "Huda", lastName: "Binte Ali", nric: "S0123456T", gender: "Female", dob: "02 Jul 1982", clinicName: "Rophi Clinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 21, firstName: "Toh", lastName: "Wen Jie", nric: "S1234567U", gender: "Male", dob: "18 Feb 1961", clinicName: "Healthway Medical", assignedDoctor: "Dr. Jenny Goh" },
    { id: 22, firstName: "Ng", lastName: "Xiao Ying", nric: "S2345678V", gender: "Female", dob: "05 Oct 1968", clinicName: "Shenton Medical Group", assignedDoctor: "Dr. Jenny Goh" },
    { id: 23, firstName: "Nur", lastName: "Sabrina", nric: "S3456789W", gender: "Female", dob: "21 Jun 1985", clinicName: "International Medical Clinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 24, firstName: "Ravi", lastName: "Subramanian", nric: "S4567890X", gender: "Male", dob: "08 Dec 1956", clinicName: "Family Medicine Clinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 25, firstName: "Tan", lastName: "Hui Qi", nric: "S5678901Y", gender: "Female", dob: "14 Apr 1965", clinicName: "Heartland Family Clinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 26, firstName: "Ahmad", lastName: "Zulkifli", nric: "S6789012Z", gender: "Male", dob: "27 Nov 1980", clinicName: "Bedok Family Medicine", assignedDoctor: "Dr. Jenny Goh" },
    { id: 27, firstName: "Low", lastName: "Yi Lin", nric: "S7890123A", gender: "Female", dob: "10 Jan 1971", clinicName: "Tampines Family Clinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 28, firstName: "Kishore", lastName: "Raj", nric: "S8901234B", gender: "Male", dob: "29 Aug 1975", clinicName: "Woodlands Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 29, firstName: "Yeo", lastName: "Jun Ming", nric: "S9012345C", gender: "Male", dob: "06 May 1989", clinicName: "Jurong Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 30, firstName: "Siti", lastName: "Aini", nric: "S0123456D", gender: "Female", dob: "22 Sep 1987", clinicName: "Bukit Batok Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 31, firstName: "Tan", lastName: "Boon Kiat", nric: "S1234567E", gender: "Male", dob: "15 Jul 1962", clinicName: "Toa Payoh Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 32, firstName: "Teo", lastName: "Pei Ling", nric: "S2345678F", gender: "Female", dob: "03 Dec 1970", clinicName: "Pasir Ris Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 33, firstName: "Nur", lastName: "Hazirah", nric: "S3456789G", gender: "Female", dob: "20 Apr 1982", clinicName: "Yishun Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 34, firstName: "Deepak", lastName: "Nair", nric: "S4567890H", gender: "Male", dob: "11 Oct 1958", clinicName: "Clementi Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 35, firstName: "Wong", lastName: "Su Mei", nric: "S5678901I", gender: "Female", dob: "26 Feb 1967", clinicName: "Ang Mo Kio Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 36, firstName: "Mohamed", lastName: "Razak", nric: "S6789012J", gender: "Male", dob: "17 Aug 1979", clinicName: "Hougang Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 37, firstName: "Koh", lastName: "Yi Fang", nric: "S7890123K", gender: "Female", dob: "04 Jan 1974", clinicName: "Sengkang Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 38, firstName: "Arun", lastName: "Kumar", nric: "S8901234L", gender: "Male", dob: "23 Nov 1977", clinicName: "Punggol Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 39, firstName: "Lim", lastName: "Boon Heong", nric: "S9012345M", gender: "Male", dob: "12 Jun 1991", clinicName: "Pioneer Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 40, firstName: "Sharifah", lastName: "Huda", nric: "S0123456N", gender: "Female", dob: "01 Mar 1984", clinicName: "Geylang Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 41, firstName: "Chan", lastName: "Wei Jie", nric: "S1234567O", gender: "Male", dob: "19 Sep 1959", clinicName: "Outram Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 42, firstName: "Quek", lastName: "Jia Min", nric: "S2345678P", gender: "Female", dob: "07 May 1966", clinicName: "Marine Parade Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 43, firstName: "Nur", lastName: "Nadiah", nric: "S3456789Q", gender: "Female", dob: "24 Aug 1981", clinicName: "Kallang Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 44, firstName: "Manoj", lastName: "Pillai", nric: "S4567890R", gender: "Male", dob: "13 Jan 1955", clinicName: "Queenstown Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 45, firstName: "Seah", lastName: "Hui Min", nric: "S5678901S", gender: "Female", dob: "30 Oct 1963", clinicName: "Bedok Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 46, firstName: "Muhammad", lastName: "Haris", nric: "S6789012T", gender: "Male", dob: "16 Jul 1978", clinicName: "Choa Chu Kang Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 47, firstName: "Lee", lastName: "Pei Wen", nric: "S7890123U", gender: "Female", dob: "09 Dec 1972", clinicName: "Bukit Merah Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 48, firstName: "Ajay", lastName: "Singh", nric: "S8901234V", gender: "Male", dob: "25 Apr 1976", clinicName: "Bishan Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 49, firstName: "Tan", lastName: "Hong Wei", nric: "S9012345W", gender: "Male", dob: "14 Feb 1992", clinicName: "Serangoon Polyclinic", assignedDoctor: "Dr. Jenny Goh" },
    { id: 50, firstName: "Amira", lastName: "Binte Salleh", nric: "S0123456X", gender: "Female", dob: "08 Nov 1985", clinicName: "Sembawang Polyclinic", assignedDoctor: "Dr. Jenny Goh" }
  ];

  const [patients, setPatients] = useState(allPatients);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const cardsPerPage = 9;

  // Filter patients based on search term - updated to search firstName, lastName, and NRIC
  const filteredPatients = patients.filter(patient =>
    patient.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    patient.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    patient.nric.toLowerCase().includes(searchTerm.toLowerCase()) ||
    patient.clinicName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const indexOfLastCard = currentPage * cardsPerPage;
  const indexOfFirstCard = indexOfLastCard - cardsPerPage;
  const currentPatients = filteredPatients.slice(indexOfFirstCard, indexOfLastCard);
  const totalPages = Math.ceil(filteredPatients.length / cardsPerPage);

  const handleRemove = (id) => {
    setPatients(patients.filter(patient => patient.id !== id));
    if (currentPatients.length === 1 && currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  // Reset to page 1 when search changes
  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    setCurrentPage(1);
  };

  const handleAddPatient = () => {
    window.location.href = '/addpatient';
  };

  return (
    <div className="flex">
      {/* Sidebar */}
      <Sidebar mcrNo="MCR123456" firstName="Jenny" />

      {/* Main Content */}
      <main className="ml-64 flex-1 p-8 bg-gray-50 min-h-screen">
        {/* Page Header with Add Patients Button */}
        <div className="mb-8">
          <div className="flex items-start justify-between">
            <Header 
              title="Assigned Patients" 
              subtitle="Manage your current patient assignments" 
            />
            <button
              onClick={() => window.location.href = '/addpatient'}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors duration-200"
            >
              <UserPlusIcon className="w-4 h-4 mr-2" />
              Add Patients
            </button>
          </div>
        </div>

        {/* Search Bar */}
        <div className="mb-6">
          <div className="relative max-w-md">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="text"
              className="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-2 focus:ring-gray-400 focus:border-transparent text-sm"
              placeholder="Search by name, NRIC, or clinic..."
              value={searchTerm}
              onChange={handleSearchChange}
            />
          </div>
        </div>

        {/* Patient Grid */}
        <PatientGrid 
          patients={currentPatients}
          onRemove={handleRemove}
          searchTerm={searchTerm}
          onAddPatient={handleAddPatient}
        />

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex justify-center items-center space-x-2 mt-8">
            <button
              onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
              disabled={currentPage === 1}
              className="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
            >
              Previous
            </button>
            
            {[...Array(totalPages)].map((_, i) => (
              <button
                key={i}
                onClick={() => setCurrentPage(i + 1)}
                className={`px-3 py-2 text-sm font-medium rounded-lg transition-colors duration-200 ${
                  currentPage === i + 1
                    ? 'text-white bg-gray-800 border border-gray-800'
                    : 'text-gray-700 bg-white border border-gray-300 hover:bg-gray-50'
                }`}
              >
                {i + 1}
              </button>
            ))}
            
            <button
              onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
              disabled={currentPage === totalPages}
              className="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
            >
              Next
            </button>
          </div>
        )}

        {/* Summary */}
        {filteredPatients.length > 0 && (
          <div className="mt-6 bg-white rounded-lg border border-gray-200 p-4">
            <p className="text-sm text-gray-600">
              Showing {currentPatients.length} of {filteredPatients.length} patients
              {searchTerm && ` matching "${searchTerm}"`}
            </p>
          </div>
        )}

        {/* Floating Add Button */}
        <button 
          onClick={() => window.location.href='/addpatient'}
          className="fixed bottom-8 right-8 w-16 h-16 bg-gray-800 hover:bg-gray-900 text-white rounded-full shadow-lg hover:shadow-xl focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-all duration-200 flex items-center justify-center group"
        >
          <PlusIcon className="w-6 h-6 group-hover:scale-110 transition-transform duration-200" />
        </button>        
      </main>
    </div>
  );
}