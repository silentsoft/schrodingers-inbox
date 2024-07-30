import React from 'react';

const Inbox = ({ emails, onEmailClick }) => (
    <div className="w-full pt-6 space-y-6 pb-4">
        {emails.length > 0 && (
            <hr/>
        )}
        <div className="space-y-3">
            {emails.map((email, index) => (
                <div key={index} className="bg-white shadow-md rounded-lg p-4 space-y-1 cursor-pointer" onClick={() => onEmailClick(email)}>
                    <h2 className="text-xl font-semibold">{email.subject}</h2>
                    <p className="text-gray-600">{email.from}</p>
                    <p className="text-gray-400">{new Date(email.date).toLocaleString()}</p>
                </div>
            ))}
        </div>
    </div>
);

export default Inbox;