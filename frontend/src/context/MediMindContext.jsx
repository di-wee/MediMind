import React, { createContext, useState } from 'react';

const MediMindContext = createContext();

export const MediMindProvider = ({ children }) => {
    const [doctorDetails, setDoctorDetails] = useState(() => {
        const stored = localStorage.getItem('doctorDetails');
        return stored ? JSON.parse(stored) : null;
    });

    React.useEffect(() => {
        if (doctorDetails) {
            localStorage.setItem('doctorDetails', JSON.stringify(doctorDetails));
        }
    }, [doctorDetails]);

    return (
        <MediMindContext.Provider value={{ doctorDetails, setDoctorDetails }}>
            {children}
        </MediMindContext.Provider>
    );
};

export default MediMindContext;
