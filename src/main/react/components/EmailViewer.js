import React from 'react';

const EmailViewer = ({ email, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={onClose}>
            <div className="bg-white shadow-md rounded-lg p-4 mb-4 max-w-3xl w-full max-h-full overflow-y-auto" onClick={(e) => e.stopPropagation()}>
                <button onClick={onClose} className="float-right bg-red-500 text-white font-semibold py-1 px-2 rounded">Close</button>
                <div className="pb-2 space-y-1">
                    <div className="text-xl font-semibold text-gray-900">
                        {email.subject}
                    </div>
                    <div className="text-gray-700">
                        <strong>From:</strong> {email.from}
                    </div>
                    <div className="text-gray-700">
                        <strong>To:</strong> {email.to}
                    </div>
                    {email.cc && (
                        <div className="text-gray-700">
                            <strong>CC:</strong> {email.cc}
                        </div>
                    )}
                </div>
                <hr/>
                <div className="text-gray-800 pt-4" dangerouslySetInnerHTML={{ __html: email.body }} />
            </div>
        </div>
    );
};

export default EmailViewer;