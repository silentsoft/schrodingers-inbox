import React, { useState } from 'react';

const ExpirationNotification = ({ claimKey, onClose }) => {
    const [showClaimKey, setShowClaimKey] = useState(false);
    const [keyCopied, setKeyCopied] = useState(false);

    const copyToClipboard = () => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(claimKey).then(() => {
                setKeyCopied(true);
                setTimeout(() => setKeyCopied(false), 1500);
            })
        } else {
            const textarea = document.createElement('textarea');
            textarea.value = claimKey;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            setKeyCopied(true);
            setTimeout(() => setKeyCopied(false), 1500);
            document.body.removeChild(textarea);
        }
    };

    const toggleClaimKeyVisibility = () => {
        setShowClaimKey(!showClaimKey);
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50">
            <div className="bg-white shadow-md rounded-lg p-4 w-96 w-full">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-semibold text-red-600">Your email has expired.</h2>
                    <button onClick={onClose} className="bg-red-500 text-white font-semibold py-1 px-2 rounded">
                        Close
                    </button>
                </div>
                <p className="mb-4">Use the Claim Key to observe your emails anytime.</p>
                <div className="space-y-2 mb-1">
                    <strong>Claim Key</strong>
                    <div className="flex items-center space-x-2">
                        <input
                            type={showClaimKey ? "text" : "password"}
                            value={claimKey}
                            readOnly
                            className="border rounded px-2 py-1 w-full"
                        />
                        <div className="inline-flex cursor-pointer" onClick={toggleClaimKeyVisibility}>
                            {showClaimKey ? (
                                <svg width="24" height="24" xmlns="http://www.w3.org/2000/svg" fill-rule="evenodd" clip-rule="evenodd">
                                    <path d="M12.01 20c-5.065 0-9.586-4.211-12.01-8.424 2.418-4.103 6.943-7.576 12.01-7.576 5.135 0 9.635 3.453 11.999 7.564-2.241 4.43-6.726 8.436-11.999 8.436zm-10.842-8.416c.843 1.331 5.018 7.416 10.842 7.416 6.305 0 10.112-6.103 10.851-7.405-.772-1.198-4.606-6.595-10.851-6.595-6.116 0-10.025 5.355-10.842 6.584zm10.832-4.584c2.76 0 5 2.24 5 5s-2.24 5-5 5-5-2.24-5-5 2.24-5 5-5zm0 1c2.208 0 4 1.792 4 4s-1.792 4-4 4-4-1.792-4-4 1.792-4 4-4z"/>
                                </svg>
                            ) : (
                                <svg width="24" height="24" xmlns="http://www.w3.org/2000/svg" fill-rule="evenodd" clip-rule="evenodd">
                                    <path d="M8.137 15.147c-.71-.857-1.146-1.947-1.146-3.147 0-2.76 2.241-5 5-5 1.201 0 2.291.435 3.148 1.145l1.897-1.897c-1.441-.738-3.122-1.248-5.035-1.248-6.115 0-10.025 5.355-10.842 6.584.529.834 2.379 3.527 5.113 5.428l1.865-1.865zm6.294-6.294c-.673-.53-1.515-.853-2.44-.853-2.207 0-4 1.792-4 4 0 .923.324 1.765.854 2.439l5.586-5.586zm7.56-6.146l-19.292 19.293-.708-.707 3.548-3.548c-2.298-1.612-4.234-3.885-5.548-6.169 2.418-4.103 6.943-7.576 12.01-7.576 2.065 0 4.021.566 5.782 1.501l3.501-3.501.707.707zm-2.465 3.879l-.734.734c2.236 1.619 3.628 3.604 4.061 4.274-.739 1.303-4.546 7.406-10.852 7.406-1.425 0-2.749-.368-3.951-.938l-.748.748c1.475.742 3.057 1.19 4.699 1.19 5.274 0 9.758-4.006 11.999-8.436-1.087-1.891-2.63-3.637-4.474-4.978zm-3.535 5.414c0-.554-.113-1.082-.317-1.562l.734-.734c.361.69.583 1.464.583 2.296 0 2.759-2.24 5-5 5-.832 0-1.604-.223-2.295-.583l.734-.735c.48.204 1.007.318 1.561.318 2.208 0 4-1.792 4-4z"/>
                                </svg>
                            )}
                        </div>
                        <div className="inline-flex cursor-pointer" onClick={copyToClipboard}>
                            {keyCopied ? (
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <title>Copied!</title>
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                                </svg>
                            ) : (
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <title>Copy</title>
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                                </svg>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ExpirationNotification;