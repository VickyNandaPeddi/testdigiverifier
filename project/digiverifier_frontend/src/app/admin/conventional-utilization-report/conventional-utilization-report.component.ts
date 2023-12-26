import { Component, OnInit } from '@angular/core';
import {
  FormGroup,
  FormControl,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';
import { timer } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

// import * as FileSaver from 'file-saver';

import { NgbCalendar, NgbDate } from '@ng-bootstrap/ng-bootstrap';
import * as XLSX from 'xlsx';
import { AuthenticationService } from 'src/app/services/authentication.service';

@Component({
  // selector: 'app-conventional-utilization-report',
  selector: 'app-conventional-utilization-report',
  templateUrl: './conventional-utilization-report.component.html',
  styleUrls: ['./conventional-utilization-report.component.scss']
})
export class ConventionalUtilizationReportComponent implements OnInit {
  pageTitle = 'Vendor Utilization Report';
  getCustomerUtilizationReport: any = [];
  //merge excel start
  getCandidateUtilizationReport: any = [];
  company = new Map<string, {}>();
  company_name: any = [];
  excel_data: any = [];
  geteKycReport: any = [];
  orgKycReport: any = [];
  getAgentUtilizationReport: any = [];
  init_agent_details: any = [];
  agent_details: any = [];
  start_date = '';
  end_date = '';
  //merge excel end
  getCustomerUtilizationReportByAgent: any = [];
  getCanididateDetailsByStatus: any = [];
  getCustID: any = [];
  custId: any = 0;
  getAgentList: any = [];
  fromDate: any;
  toDate: any;
  setfromDate: any;
  settoDate: any;
  getToday: NgbDate;
  getMinDate: any;
  kyc: Boolean = false;
  hideLoadingBtn: Boolean = false;
  utilizationReportClick = new FormGroup({
    fromDate: new FormControl('', Validators.required),
    toDate: new FormControl('', Validators.required),
    organizationIds: new FormControl('', Validators.required),
    statusCode: new FormControl('', Validators.required),
    vendorStatusmasterId: new FormControl('', Validators.required),
  });
  utilizationReportFilter = new FormGroup({
    fromDate: new FormControl('', Validators.required),
    toDate: new FormControl('', Validators.required),
    organizationIds: new FormControl('', Validators.required),
  });
  sheets = [
    'IDENTITY CHECK',
    'GLOBAL DATABASE CHECK',
    'EDUCATION',
    'EMPLOYMENT CHECK',
    'CRIMINAL CHECK',
    'ADDRESS CHECK'
  ];
  responseChain : boolean[] = [];
  
  excelBuffer: any;
  fileName = 'export.xlsx';
  initToday: string;

  exportexcel(): void {
    const fileName = 'Vendor Utilization Report.xlsx';

    let wb = XLSX.utils.book_new();
    let ws_IDENTITYCheck: any;
    let ws_GLOBALDATABASECHECK: any;
    let ws_EDUCATIONCHECK: any;
    let ws_EMPLOYMENTCHECK: any;
    let ws_CRIMINALCHECK: any;
    let ws_ADDRESSCHECK: any;
    let IDENTITYCHECK = 0;
    let GLOBALDATABASECHECK = 0;
    let EDUCATIONCHECK = 0;
    let EMPLOYMENTCHECK = 0;
    let CRIMINALCHECK = 0;
    let ADDRESSCHECK = 0;
    let OLD_IDENTITYCHECK_LEN = 0;
    let OLD_GLOBALDATABASECHECK_LEN = 0;
    let OLD_EDUCATIONCHECK_LEN = 0;
    let OLD_EMPLOYMENTCHECK_LEN = 0;
    let OLD_CRIMINALCHECK_LEN = 0;
    let OLD_ADDRESSCHECK_LEN = 0;

    const columnWidths = [
      { wpx: 150 }, // Column 1 width is set to 100 pixels
      { wpx: 200 }, // Column 2 width is set to 150 pixels
      { wpx: 80 }, // Column 3 width is set to 120 pixels
      { wpx: 200 },
      { wpx: 150 },
      { wpx: 100 },
      { wpx: 200 },
      { wpx: 80 },
      { wpx: 80 },
      { wpx: 200 },
      { wpx: 80 }, // already used till here

      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 }
    ];

    const summaryColumnWidths = [
      { wpx: 120 }, // Column 1 width is set to 100 pixels
      { wpx: 120 }, // Column 2 width is set to 150 pixels
      { wpx: 100 }, // Column 3 width is set to 120 pixels
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
      { wpx: 100 },
    ];

    // let ws_OVERALLSummary = XLSX.utils.json_to_sheet(this.getCustomerUtilizationReport);
    let element = document.getElementById('excel-table');
    let ws_OVERALLSummary = XLSX.utils.table_to_sheet(element);
    ws_OVERALLSummary['!cols'] = summaryColumnWidths;

    let ws_ekycReport = XLSX.utils.json_to_sheet(this.geteKycReport);
    console.log('this.company', this.company);
    this.company.forEach((value: any = [], key: string) => {
      const filter_key = key.slice(0, 30);
      if (key.includes('IDENTITY CHECK')) {
        for (let val in value) {
          delete value[val].dateOfBirth;
          delete value[val].fatherName;
          delete value[val].gender;
          delete value[val].contactNumber;
          // value[val].Date = value[val].statusDate
          delete value[val].univ1;
          delete value[val].courseName1;
          delete value[val].yop1;
          delete value[val].endDate1;
          delete value[val].result1;
          delete value[val].univ2;
          delete value[val].courseName2;
          delete value[val].yop2;
          delete value[val].endDate2;
          delete value[val].result2;
          delete value[val].univ3;
          delete value[val].courseName3;
          delete value[val].yop3;
          delete value[val].endDate3;
          delete value[val].result3;
        }
        if (IDENTITYCHECK == 0) {
          ws_IDENTITYCheck = XLSX.utils.json_to_sheet(value);
          IDENTITYCHECK = 1;
          OLD_IDENTITYCHECK_LEN = OLD_IDENTITYCHECK_LEN + value.length;
        } else {
          console.log('Inside Non zero', key, OLD_IDENTITYCHECK_LEN, value);
          XLSX.utils.sheet_add_json(ws_IDENTITYCheck, value, {
            skipHeader: false,
            origin: `A${OLD_IDENTITYCHECK_LEN + 2}`,
          });
          OLD_IDENTITYCHECK_LEN = OLD_IDENTITYCHECK_LEN + value.length;
        }
        ws_IDENTITYCheck['!cols'] = columnWidths;
      } else if (key.includes('GLOBAL DATABASE CHECK')) {
        for (let val in value) {
          delete value[val].aadharNumber;
          delete value[val].panNumber;
          delete value[val].contactNumber;

          delete value[val].univ1;
          delete value[val].courseName1;
          delete value[val].yop1;
          delete value[val].endDate1;
          delete value[val].result1;
          delete value[val].univ2;
          delete value[val].courseName2;
          delete value[val].yop2;
          delete value[val].endDate2;
          delete value[val].result2;
          delete value[val].univ3;
          delete value[val].courseName3;
          delete value[val].yop3;
          delete value[val].endDate3;
          delete value[val].result3;
        }
        if (GLOBALDATABASECHECK == 0) {
          console.log('Inside zero', key, value.length, value);
          ws_GLOBALDATABASECHECK = XLSX.utils.json_to_sheet(value);
          GLOBALDATABASECHECK = 1;
          OLD_GLOBALDATABASECHECK_LEN =
            OLD_GLOBALDATABASECHECK_LEN + value.length;
        } else {
          console.log(
            'Inside Non zero',
            key,
            OLD_GLOBALDATABASECHECK_LEN,
            value
          );
          XLSX.utils.sheet_add_json(ws_GLOBALDATABASECHECK, value, {
            skipHeader: false,
            origin: `A${OLD_GLOBALDATABASECHECK_LEN + 2}`,
          });
          OLD_GLOBALDATABASECHECK_LEN =
            OLD_GLOBALDATABASECHECK_LEN + value.length;
        }
        ws_GLOBALDATABASECHECK['!cols'] = columnWidths;
      } else if (key.includes('EDUCATION')) {
        for (let val in value) {
          delete value[val].aadharNumber;
          delete value[val].panNumber;
          delete value[val].dateOfBirth;
          delete value[val].fatherName;
          delete value[val].gender;
        }
        if (EDUCATIONCHECK == 0) {
          console.log('Inside zero', key, value.length, value);
          ws_EDUCATIONCHECK = XLSX.utils.json_to_sheet(value);
          EDUCATIONCHECK = 1;
          OLD_EDUCATIONCHECK_LEN = OLD_EDUCATIONCHECK_LEN + value.length;
        } else {
          console.log('Inside Non zero', key, OLD_EDUCATIONCHECK_LEN, value);
          XLSX.utils.sheet_add_json(ws_EDUCATIONCHECK, value, {
            skipHeader: false,
            origin: `A${OLD_EDUCATIONCHECK_LEN + 2}`,
          });
          OLD_EDUCATIONCHECK_LEN = OLD_EDUCATIONCHECK_LEN + value.length;
        }
        ws_EDUCATIONCHECK['!cols'] = columnWidths;
      } else if (key.includes('EMPLOYMENT CHECK')) {
        if (EMPLOYMENTCHECK == 0) {
          ws_EMPLOYMENTCHECK = XLSX.utils.json_to_sheet(value);
          EMPLOYMENTCHECK = 1;
          OLD_EMPLOYMENTCHECK_LEN = OLD_EMPLOYMENTCHECK_LEN + value.length;
        } else {
          XLSX.utils.sheet_add_json(ws_EMPLOYMENTCHECK, value, {
            skipHeader: false,
            origin: `A${OLD_EMPLOYMENTCHECK_LEN + 2}`,
          });
          OLD_EMPLOYMENTCHECK_LEN = OLD_EMPLOYMENTCHECK_LEN + value.length;
        }
        ws_EMPLOYMENTCHECK['!cols'] = columnWidths;
      } else if (key.includes('CRIMINAL CHECK')) {
        for (let val in value) {
          delete value[val].aadharNumber;
          delete value[val].panNumber;
          delete value[val].contactNumber;

          delete value[val].univ1;
          delete value[val].courseName1;
          delete value[val].yop1;
          delete value[val].endDate1;
          delete value[val].result1;
          delete value[val].univ2;
          delete value[val].courseName2;
          delete value[val].yop2;
          delete value[val].endDate2;
          delete value[val].result2;
          delete value[val].univ3;
          delete value[val].courseName3;
          delete value[val].yop3;
          delete value[val].endDate3;
          delete value[val].result3;
        }
        if (CRIMINALCHECK == 0) {
          ws_CRIMINALCHECK = XLSX.utils.json_to_sheet(value);
          CRIMINALCHECK = 1;
          OLD_CRIMINALCHECK_LEN = OLD_CRIMINALCHECK_LEN + value.length;
        } else {
          XLSX.utils.sheet_add_json(ws_CRIMINALCHECK, value, {
            skipHeader: false,
            origin: `A${OLD_CRIMINALCHECK_LEN + 2}`,
          });
          OLD_CRIMINALCHECK_LEN = OLD_CRIMINALCHECK_LEN + value.length;
        }
        ws_CRIMINALCHECK['!cols'] = columnWidths;
      } else if (key.includes('ADDRESS CHECK')) {
        for (let val in value) {
          delete value[val].aadharNumber;
          delete value[val].panNumber;
          delete value[val].contactNumber;

          delete value[val].univ1;
          delete value[val].courseName1;
          delete value[val].yop1;
          delete value[val].endDate1;
          delete value[val].result1;
          delete value[val].univ2;
          delete value[val].courseName2;
          delete value[val].yop2;
          delete value[val].endDate2;
          delete value[val].result2;
          delete value[val].univ3;
          delete value[val].courseName3;
          delete value[val].yop3;
          delete value[val].endDate3;
          delete value[val].result3;
        }
        if (ADDRESSCHECK == 0) {
          ws_ADDRESSCHECK = XLSX.utils.json_to_sheet(value);
          ADDRESSCHECK = 1;
          OLD_ADDRESSCHECK_LEN = OLD_ADDRESSCHECK_LEN + value.length;
        } else {
          XLSX.utils.sheet_add_json(ws_ADDRESSCHECK, value, {
            skipHeader: false,
            origin: `A${OLD_ADDRESSCHECK_LEN + 2}`,
          });
          OLD_ADDRESSCHECK_LEN = OLD_ADDRESSCHECK_LEN + value.length;
        }
        ws_ADDRESSCHECK['!cols'] = columnWidths;
      }
      
    });

    XLSX.utils.book_append_sheet(wb, ws_OVERALLSummary, 'OVERALLSUMMARY');

        // Convert the table to an Excel sheet
    // Iterate through the cells in the sheet and set the cell format to "General"
    for (let cellAddress in ws_OVERALLSummary) {   
      if (ws_OVERALLSummary.hasOwnProperty(cellAddress)) {
             let cell = ws_OVERALLSummary[cellAddress]; // Check if the cell contains a date value (you may need to adjust this condition)    
             if (cellAddress == 'A1') {       // Set the cell format to "General"      
              cell.t = 'n'; // 'n' stands for number format (General)    
              cell.z = 'dd/mm/yy';
              cell.v = 'Start Date : ' + this.start_date;
              console.log('cell a1', cell, cellAddress)
            }   

            if (cellAddress == 'B1') {       // Set the cell format to "General"      
              cell.t = 'n'; // 'n' stands for number format (General)    
              cell.z = 'dd/mm/yy';
              cell.v = 'End Date : ' + this.end_date;
              console.log('cell a1', cell, cellAddress)
            }
      } 
    } // Create a new workbook and add the sheet to it

    XLSX.utils.book_append_sheet(wb, ws_IDENTITYCheck, 'IDENTITY CHECK');
    XLSX.utils.book_append_sheet(wb, ws_GLOBALDATABASECHECK, 'GLOBAL DATABASE CHECK');
    XLSX.utils.book_append_sheet(wb, ws_EDUCATIONCHECK, 'EDUCATION CHECK');
    XLSX.utils.book_append_sheet(wb, ws_EMPLOYMENTCHECK, 'EMPLOYMENT CHECK');
    XLSX.utils.book_append_sheet(wb, ws_CRIMINALCHECK, 'CRIMINAL CHECK');
    XLSX.utils.book_append_sheet(wb, ws_ADDRESSCHECK, "ADDRESS CHECK");
    XLSX.writeFile(wb, fileName);
  }

  onfromDate(event: any) {
    let year = event.year;
    let month = event.month <= 9 ? '0' + event.month : event.month;
    let day = event.day <= 9 ? '0' + event.day : event.day;
    let finalDate = day + '/' + month + '/' + year;
    this.fromDate = finalDate;
    this.getMinDate = { day: +day, month: +month, year: +year };
  }
  ontoDate(event: any) {
    let year = event.year;
    let month = event.month <= 9 ? '0' + event.month : event.month;
    let day = event.day <= 9 ? '0' + event.day : event.day;
    let finalDate = day + '/' + month + '/' + year;
    this.toDate = finalDate;
  }

  constructor(
    private customers: CustomerService,
    private navrouter: Router,
    calendar: NgbCalendar,
    private route: ActivatedRoute,
    public authService: AuthenticationService
  ) {
    this.getToday = calendar.getToday();
    this.customers.getCustomersBill().subscribe((data: any) => {
      this.getCustID = data.data;
    });

    let rportData = {
      userId: localStorage.getItem('userId'),
    };

    this.customers.getVendorUtilizationReport().subscribe((data: any) => {
      this.getCustomerUtilizationReport = data.data.reportResponseDtoList;
      let getfromDate = data.data.fromDate.split('/');
      this.setfromDate = {
        day: +getfromDate[0],
        month: +getfromDate[1],
        year: +getfromDate[2],
      };
      this.getMinDate = this.setfromDate;

      let gettoDate = data.data.toDate.split('/');
      this.settoDate = {
        day: +gettoDate[0],
        month: +gettoDate[1],
        year: +gettoDate[2],
      };
      console.log(
        'getfromDate, gettoDate',
        this.getMinDate,
        this.settoDate,
        this.fromDate,
        this.toDate
      );

      this.start_date = 'No Date Filter'; //data.data.fromDate!=null?data.data.fromDate.split('-').join('/'):''
      this.end_date = 'No Date Filter'; //data.data.toDate!=null?data.data.toDate.split('-').join('/'):''

      this.start_date =
      data.data.fromDate != null
        ? data.data.fromDate.split('-').join('/')
        : '';
      this.end_date =
      data.data.toDate != null
        ? data.data.toDate.split('-').join('/')
        : '';

      console.log(
        'getCustomerUtilizationReport',
        this.getCustomerUtilizationReport
      );
      let company = new Map<string, {}>();

      this.utilizationReportClick.patchValue({
        fromDate:
          data.data.fromDate != null
            ? data.data.fromDate.split('-').join('/')
            : '',
        toDate:
          data.data.toDate != null ? data.data.toDate.split('-').join('/') : '',
        organizationIds: [88],
        agentIds: [],
      });

      this.utilizationReportFilter.patchValue({
        fromDate: this.setfromDate,
        toDate: this.settoDate,
      });
      this.fromDate = data.data.fromDate != null ? data.data.fromDate : '';
      this.toDate = data.data.toDate != null ? data.data.toDate : '';

      if (authService.roleMatch(['ROLE_AGENTHR'])) {
        const navURL = 'admin/customerUtilizationAgent/';
        this.navrouter.navigate([navURL], {
          queryParams: {
            fromDate: this.fromDate,
            toDate: this.toDate,
            organizationIds: localStorage.getItem('orgID'),
            statusCode: 'agent',
          },
        });
      }
    });

    // for (let item in this.getCustomerUtilizationReport){
    this.hideLoadingBtn = false;
    this.responseChain = [];
    this.sheets.forEach((item) => {
      let features: any = {};
      console.log('item', item);

      // Object.keys(this.getCustomerUtilizationReport[item]).find((key)=>{
      // if (key.includes('Code')){
      const statusCode = item;
      const agentIds = this.route.snapshot.queryParamMap.get('agentIds');
      const isAgent = this.route.snapshot.queryParamMap.get('isAgent');
      let agentIdsArray: any = [];
      agentIdsArray.push(agentIds);
      let vendorStatusmasterId = 0;

      if (isAgent == 'true') {
        this.utilizationReportClick.patchValue({
          organizationIds: [88],
          statusCode: statusCode,
          agentIds: agentIdsArray,
          vendorStatusmasterId: vendorStatusmasterId,
        });
      } else {
        this.utilizationReportClick.patchValue({
          organizationIds: [88],
          statusCode: statusCode,
          agentIds: [],
          vendorStatusmasterId: vendorStatusmasterId,
        });
      }
      this.customers
        .getVendorDetailsByStatus(this.utilizationReportClick.value)
        .subscribe((result: any) => {
          if (result['data'] != null && result.data.length > 0) {
            // this.getCandidateUtilizationReport=result.data.candidateDetailsDto;
            // features[result['data']['statusCode']] = result.data.candidateDetailsDto;
            this.company.set(result['data'] + ', ' + statusCode, result.data);
          }

          this.responseChain.push(true);

          if (this.responseChain.length -1 == this.sheets.length - 1 && !this.responseChain.includes(false)){
            this.hideLoadingBtn = true;
            console.log(this.responseChain)
          }
        }, err => {
          console.log(err.console.error());
          this.responseChain.push(true);
        });
    });

    this.getToday = calendar.getToday();
    let inityear = this.getToday.year;
    let initmonth =
      this.getToday.month <= 9
        ? '0' + this.getToday.month
        : this.getToday.month;
    let initday =
      this.getToday.day <= 9 ? '0' + this.getToday.day : this.getToday.day;
    let initfinalDate = initday + '/' + initmonth + '/' + inityear;
    this.initToday = initfinalDate;
  }

  getData(custId: any, statusCode: any) {
    let organizationIds: any = [];
    organizationIds.push(custId);
    console.log('this.fromDate, this.toDate', this.fromDate, this.toDate);
    this.utilizationReportClick.patchValue({
      fromDate: this.fromDate,
      toDate: this.toDate,
      organizationIds: organizationIds,
      statusCode: statusCode,
    });
    if (statusCode == 'agent') {
      const navURL = 'admin/customerUtilizationAgent/';
      this.navrouter.navigate([navURL], {
        queryParams: {
          fromDate: this.fromDate,
          toDate: this.toDate,
          organizationIds: organizationIds,
          statusCode: statusCode,
        },
      });
    } else {
      const navURL = 'admin/customerUtilizationCandidate/';
      this.navrouter.navigate([navURL], {
        queryParams: {
          fromDate: this.fromDate,
          toDate: this.toDate,
          organizationIds: organizationIds,
          statusCode: statusCode,
        },
      });
    }
  }

  getcustId(id: any) {
    this.custId = id;
  }
  onSubmitFilter(utilizationReportFilter: FormGroup) {
    this.fromDate = this.fromDate != null ? this.fromDate : '';
    this.toDate = this.toDate != null ? this.toDate : '';
    let organizationIds: any = [];
    organizationIds.push(this.custId);
    this.utilizationReportFilter.patchValue({
      fromDate: this.fromDate,
      toDate: this.toDate,
      organizationIds: organizationIds,
    });
    this.customers
      .postVendorUtilizationReport(this.utilizationReportFilter.value)
      .subscribe((data: any) => {
        if (data.outcome === true) {
          this.getCustomerUtilizationReport = data.data.reportResponseDtoList;
          console.log(
            'this.getCustomerUtilizationReport',
            this.getCustomerUtilizationReport
          );
          this.start_date =
            data.data.fromDate != null
              ? data.data.fromDate.split('-').join('/')
              : '';
          this.end_date =
            data.data.toDate != null
              ? data.data.toDate.split('-').join('/')
              : '';

          //merge excel start
          this.company = new Map<string, {}>();

          console.log('filter this.company', this.company);
          timer(5000).subscribe((x) => {
            console.log('your_action_code_here', this.company);

            let count = 0;
            this.company.forEach((value: any = [], key: string) => {
              var agent_dict: any = {};
              if (key.includes('AGENT') && count != 0) {
                agent_dict['key'] = key;
                agent_dict['value'] = value;
                this.agent_details.push(agent_dict);
              }
              count += 1;
            });
            console.log('this.agent_details', this.agent_details);
          });

          //merge excel end

          let getfromDate = data.data.fromDate.split('/');
          this.setfromDate = {
            day: +getfromDate[0],
            month: +getfromDate[1],
            year: +getfromDate[2],
          };
          let gettoDate = data.data.toDate.split('/');
          this.settoDate = {
            day: +gettoDate[0],
            month: +gettoDate[1],
            year: +gettoDate[2],
          };
          this.utilizationReportFilter.patchValue({
            fromDate: this.setfromDate,
            toDate: this.settoDate,
          });
        } else {
          Swal.fire({
            title: data.message,
            icon: 'warning',
          });
        }
      });

    this.hideLoadingBtn = false;
    this.responseChain = [];
    // for (let item in this.getCustomerUtilizationReport){
    this.sheets.forEach((item) => {
      var features: any = {};
      // console.log("item",this.getCustomerUtilizationReport[item].name);

      // Object.keys(this.getCustomerUtilizationReport[item]).find((key)=>{
      // if (key.includes('Code')){
      const statusCode = item;
      // console.log("statusCode type *****",typeof(statusCode), statusCode);
      const agentIds = this.route.snapshot.queryParamMap.get('agentIds');
      const isAgent = this.route.snapshot.queryParamMap.get('isAgent');
      // console.log("agentIds isAgent",agentIds,isAgent);
      let agentIdsArray: any = [];
      agentIdsArray.push(agentIds);

      if (isAgent == 'true') {
        this.utilizationReportClick.patchValue({
          fromDate:
            this.fromDate != null ? this.fromDate.split('-').join('/') : '',
          toDate: this.toDate != null ? this.toDate.split('-').join('/') : '',
          organizationIds: [88],
          statusCode: statusCode,
          agentIds: agentIdsArray,
        });
      } else {
        this.utilizationReportClick.patchValue({
          fromDate:
            this.fromDate != null ? this.fromDate.split('-').join('/') : '',
          toDate: this.toDate != null ? this.toDate.split('-').join('/') : '',
          organizationIds: [88],
          statusCode: statusCode,
          agentIds: [],
        });
      }
      // console.log("this.customers.getCanididateDetailsByStatus",this.utilizationReportClick);
      this.customers
        .getVendorDetailsByStatus(this.utilizationReportClick.value)
        .subscribe((result: any) => {
          if (result['data'] != null && result.data.length > 0) {
            this.company.set(result['data'] + ', ' + statusCode, result.data);
          }

          this.responseChain.push(true);

          if (this.responseChain.length -1 == this.sheets.length - 1 && !this.responseChain.includes(false)){
            this.hideLoadingBtn = true;
            console.log(this.responseChain)
          }
        }, err => {
          console.log(err.console.error());
          this.responseChain.push(true);
        });
    });
  }

  filterToday() {
    this.customers.setFromDate(this.initToday);
    this.customers.setToDate(this.initToday);
    this.fromDate = this.initToday;
    this.toDate = this.initToday;

    let organizationIds: any = [];
    organizationIds.push(this.custId);
    this.utilizationReportFilter.patchValue({
      fromDate: this.fromDate,
      toDate: this.toDate,
      organizationIds: organizationIds,
    });
    this.onSubmitFilter(this.utilizationReportFilter);
  }

  filterLastMonth() {
    let date = new Date();
    date.setMonth(date.getMonth() - 1);
    let firstDayOfMonth = new Date(date.getFullYear(), date.getMonth(), 2);
    let lastDayOfMonth = new Date(date.getFullYear(), date.getMonth() + 1, 1);
    let fromDateString = firstDayOfMonth.toISOString().split('T')[0];
    let toDateString = lastDayOfMonth.toISOString().split('T')[0];
  
    let getInputFromDate: any = fromDateString.split('-');
    let finalInputFromDate =
      getInputFromDate[2] +
      '/' +
      getInputFromDate[1] +
      '/' +
      getInputFromDate[0];

      let getInputToDate: any = toDateString.split('-');
      let finalInputToDate =
        getInputToDate[2] +
        '/' +
        getInputToDate[1] +
        '/' +
        getInputToDate[0];
  
    this.fromDate = finalInputFromDate;
    this.toDate = finalInputToDate;

    this.customers.setFromDate(finalInputFromDate);
    this.customers.setToDate(finalInputToDate); 

    let organizationIds: any = [];
    organizationIds.push(this.custId);
    this.utilizationReportFilter.patchValue({
      fromDate: this.fromDate,
      toDate: this.toDate,
      organizationIds: organizationIds,
    });
  
    this.onSubmitFilter(this.utilizationReportFilter);
  }
  
  filterMonthToDate() {
    let currentDate = new Date();
    let firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 2);
    let fromDateString = firstDayOfMonth.toISOString().split('T')[0];
    let toDateString = currentDate.toISOString().split('T')[0];
  
    let getInputFromDate: any = fromDateString.split('-');
    let finalInputFromDate =
      getInputFromDate[2] +
      '/' +
      getInputFromDate[1] +
      '/' +
      getInputFromDate[0];
  
    this.fromDate = finalInputFromDate;
    this.toDate = this.initToday;

    this.customers.setFromDate(finalInputFromDate);
    this.customers.setToDate(this.initToday); 

    let organizationIds: any = [];
    organizationIds.push(this.custId);
    this.utilizationReportFilter.patchValue({
      fromDate: this.fromDate,
      toDate: this.toDate,
      organizationIds: organizationIds,
    });
  
    this.onSubmitFilter(this.utilizationReportFilter);
  }

  ngOnInit(): void {}
}
