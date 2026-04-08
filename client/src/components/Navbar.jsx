import React from 'react';
import { NavLink } from 'react-router-dom';
import '../styles/Navbar.css';

export default function Navbar() {
  return (
    <nav className="navbar top-section">
      <NavLink to="/">Test Items</NavLink>
      <NavLink to="/admin">Admin</NavLink>
      <NavLink to="/prereq-vis">Course Prerequisites</NavLink>
    </nav>
  );
}
