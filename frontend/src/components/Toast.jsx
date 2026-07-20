import { useState, useEffect } from 'react';

export default function Toast({ message, type = 'success', onClose }) {
  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => onClose?.(), 4000);
      return () => clearTimeout(timer);
    }
  }, [message, onClose]);

  if (!message) return null;
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };

  return (
    <div className="toast-container">
      <div className={`toast toast-${type}`} onClick={onClose} style={{ cursor: 'pointer' }}>
        <span>{icons[type]}</span>
        <span>{message}</span>
      </div>
    </div>
  );
}
