%define major_ver __MAJOR_VER__
%define minor_ver __MINOR_VER__
%define module_name __MODULE_NAME__
%define service_name __SERVICE_NAME__
%define company_prefix __COMPANY_NAME__
%define gooddata /gooddata/
%define gooddatalib /gooddata/java/%{module_name}

Name: %{company_prefix}-%{service_name}-%{module_name}
Summary: %{module_name} RPM Installer
Version: %{major_ver}.%{minor_ver}
Release: 1%{?dist}
License: Copyright (C) 2012-2013 Good Data. All rights reserved.
Packager: Good Data, Consulting Service Team
#Vendor:
Group: Applications
Source: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/build-root-%{name}
#Requires: %{company_prefix}-%{service_name}-module-pkg >= 1.0
#Provides: libc.so.6(GLIBC_PRIVATE)

Prefix: /gooddata
Url: http://www.gooddata.com/

%description
%{module_name} is the common lib to hold URL in a pre-defined 
ADT. It provides the data interface and structure, and certain level
of marshalling/demarshalling for further usage.

%prep
echo "ok - building %{name}-%{major_ver}-%{minor_ver}"
if [ "$RPM_BUILD_ROOT" != "/" ] ; then
  echo "ok - removing old files [$RPM_BUILD_ROOT]"
  rm -rf $RPM_BUILD_ROOT
fi

%setup -q -n %{module_name}
%{__mkdir} -p $RPM_BUILD_ROOT%{gooddata}
%{__mkdir} -p $RPM_BUILD_ROOT%{gooddatalib}
%{__mkdir} -p $RPM_BUILD_ROOT%{gooddatalib}/%{major_ver}/
%{__mkdir} -p $RPM_BUILD_ROOT%{gooddatalib}/%{major_ver}/lib/

%install
cp -p dist/lib/* $RPM_BUILD_ROOT%{gooddatalib}/%{major_ver}/lib/

%clean
echo "ok - cleaning up temporary files"
[ "$RPM_BUILD_ROOT" != "/" ] && rm -rf $RPM_BUILD_ROOT

%files
%defattr(0755,root,root,0755)
%{gooddatalib}/%{major_ver}/

%pre
# Create Other Required Folders
mkdir -p %{gooddata}
mkdir -p %{gooddatalib}
mkdir -p %{gooddatalib}/%{major_ver}/
mkdir -p %{gooddatalib}/%{major_ver}/lib/

%post
# Nothing to do for post0installation
echo "ok - complete installing %{name}-%{major_ver}.%{minor_ver}-%{release}"

%preun
if [ -d "%{gooddatalib}/%{major_ver}/" ] ; then
    echo "ok - uninstall removing %{gooddatalib}/%{major_ver} folder ..."
    rm -rf "%{gooddatalib}/%{major_ver}/"
fi

%changelog
* Fri Dec 09 2011 Andrew Lee 20111209
- Initial Creation of spec file


