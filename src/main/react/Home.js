import React, { useState, useEffect } from 'react';
import Account from './components/Account';
import Inbox from './components/Inbox';
import EmailViewer from './components/EmailViewer';
import ExpirationNotification from './components/ExpirationNotification';
import axios from 'axios';
import Footer from './Footer';

export default function Home(props) {
    const [emails, setEmails] = useState([]);
    const [selectedEmail, setSelectedEmail] = useState(null);
    const [claimKey, setClaimKey] = useState(null);
    const [emailAddress, setEmailAddress] = useState(null);
    const [showClaimKey, setShowClaimKey] = useState(false);
    const [showExpired, setShowExpired] = useState(false);
    const [loading, setLoading] = useState(false);
    const [eventSource, setEventSource] = useState(null);
    const [inputClaimKey, setInputClaimKey] = useState('');

    const handleGenerateEmail = async () => {
        if (loading) return;

        setLoading(true);
        setEmailAddress(null);
        setClaimKey(null);
        setEmails([]);

        if (eventSource) {
            eventSource.close();
        }

        try {
            const response = await axios.post(`${process.env.REACT_APP_URI}/generate`);
            setClaimKey(response.data.key);
        } catch (error) {
            console.error('Error generating email:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleClaimKeySubmit = async () => {
        setClaimKey(inputClaimKey);
    };

    const subscribeToEmails = (claimKey) => {
        const newEventSource = new EventSource(`${process.env.REACT_APP_URI}/subscribe/${claimKey}`);
        setEventSource(newEventSource);

        newEventSource.addEventListener('email', (event) => {
            setEmailAddress(event.data);
        });
        newEventSource.addEventListener('email:received', (event) => {
            const newEmail = JSON.parse(event.data);
            setEmails(prevEmails => [newEmail, ...prevEmails]);
        });
        newEventSource.addEventListener('email:expired', () => {
            newEventSource.close();
            setShowExpired(true);
        });
    };

    const toggleClaimKeyVisibility = () => {
        setShowClaimKey(!showClaimKey);
    };

    const handleEmailClick = (email) => {
        setSelectedEmail(email);
    };

    const handleCloseViewer = () => {
        setSelectedEmail(null);
    };

    const handleCloseNotification = () => {
        setShowExpired(false);
        window.location.reload();
    };

    useEffect(() => {
        if (claimKey) {
            subscribeToEmails(claimKey);
        }
        return () => {
            if (eventSource) {
                eventSource.close();
            }
        };
    }, [claimKey]);

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="flex flex-col items-center w-96">
                <div className="flex flex-col items-center space-y-4 mb-6">
                    <img src="/logo.png" alt="Logo" className="h-32 w-32" />
                    <h1 className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-br from-cyan-400 to-blue-600">Schrodinger's Inbox</h1>
                    <span className="font-medium text-center text-gray-400 font-black">
                        Your emails exist and don't exist until you observe them. A quantum leap in temporary email services.
                    </span>
                </div>
                {emailAddress ? (
                    <Account
                        emailAddress={emailAddress}
                        claimKey={claimKey}
                        showClaimKey={showClaimKey}
                        toggleClaimKeyVisibility={toggleClaimKeyVisibility}
                    />
                ) : (
                    <>
                        <button
                            className={`bg-blue-500 text-white font-semibold py-2 px-4 rounded mb-4 ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
                            onClick={handleGenerateEmail}
                            disabled={loading}
                        >
                            {loading ? (
                                <svg className="animate-spin h-5 w-5 mr-3 border-t-2 border-white rounded-full" viewBox="0 0 24 24"></svg>
                            ) : (
                                'Generate Email'
                            )}
                        </button>
                        {!loading && (
                            <div className="text-center mt-4">
                                <p className="text-gray-700 mb-2">... or observe using Claim Key</p>
                                <div className="flex items-center justify-center">
                                    <input
                                        type="password"
                                        onChange={(e) => setInputClaimKey(e.target.value)}
                                        className="border rounded px-2 py-1 mr-2"
                                        placeholder="Enter Claim Key"
                                    />
                                    <button
                                        className="bg-green-500 text-white font-semibold py-1 px-4 rounded"
                                        onClick={handleClaimKeySubmit}
                                    >
                                        Observe
                                    </button>
                                </div>
                            </div>
                        )}
                    </>
                )}
                <Inbox emails={emails} onEmailClick={handleEmailClick} />
                {selectedEmail && (
                    <EmailViewer email={selectedEmail} onClose={handleCloseViewer} />
                )}
                {showExpired && (
                    <ExpirationNotification
                        claimKey={claimKey}
                        onClose={handleCloseNotification}
                    />
                )}
                <Footer/>
            </div>
        </div>
    );
}