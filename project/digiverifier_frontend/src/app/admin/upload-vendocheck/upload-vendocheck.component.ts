import { Component, ElementRef, NgZone, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators, FormArray } from '@angular/forms';
import { Router } from '@angular/router';
import { ModalDismissReasons, NgbCalendar, NgbDate, NgbDateStruct, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { CandidateService } from 'src/app/services/candidate.service';
import Swal from 'sweetalert2';

import { CustomerService } from '../../services/customer.service';
import { LoaderService } from "../../services/loader.service";

import * as am4charts from "@amcharts/amcharts4/charts";
import * as am4core from "@amcharts/amcharts4/core";
import { OrgadminDashboardService } from 'src/app/services/orgadmin-dashboard.service';

@Component({
  selector: 'app-upload-vendocheck',
  templateUrl: './upload-vendocheck.component.html',
  styleUrls: ['./upload-vendocheck.component.scss']
})
export class UploadVendocheckComponent implements OnInit {
  private chart: am4charts.XYChart | undefined;
  getReportDeliveryStatCodes: any;
  CharReportDelivery: any=[];
  CharReportDeliveryData: any=[];
  containerStat:boolean = false;
  stat_linkAdminApproval:boolean = false;
  stat_linkCandidateReport:boolean = false;
  stat_linkConventional:boolean = false;
  finalreport:boolean=false;
  startdownload:boolean=false;
  startpredownload:boolean=false;
  isCBadmin:boolean = false;

  pageTitle = 'Vendor Management';
  vendorchecksupload: any = [];
  vendoruser: any
  userID: any;
  getVendorID: any = [];
  candidateId: any;
  candidateCode: any;
  sourceId: any;
  candidateName: any = [];
  userName: any = [];
  sourceName: any = [];
  vendorId: any;
  getColors: any = [];
  colorid: any;
  fromDate: any;
  toDate: any;
  setfromDate: any;
  settoDate: any;
  getToday: NgbDate;
  getMinDate: any;
  start_date = "";
  end_date = "";
  proofDocumentNew: any;
  venderAttributeValue: any[] = [];
  venderAttributeCheckMapped: any[] = [];
  closeModal: string | undefined;
  filteredData: any[] = [];
  candidateNameFilter: string = '';
  tep: any = [1];
  // vendorlist:any;
  tmp: any;
  orgID: any;
  // currentPageIndex: number = 0;
  // currentPage = 1;
  // pageSize: number = 10;

  pageNumber: number = 0;
  pageSize: number = 50;

  currentPage = 1;
  currentPageIndex: number = 0;

  formMyProfile: any;
  createdOnDate: any;
  selectedFile: File | null = null;
  isVendorAttributeForm: boolean | undefined;
  civilProceedingsCount: number = 0;
  globalAttributeValue: any[] = [];
  indiaAttributeValue: any[] = [];

  activeForm: string = '';
  selectedGlobalAttributeValue: { label: string; value: string }[] = [];
  globaldataBaseCheck: any;
  globalCheckType: string = 'GLOBAL';

  attributeMap: Map<string, any[]> = new Map<string, any[]>();
  selectedTab: any;
  selectedIndiaAttributeValue: any[] = [];

  totalPages: number = 0;

  vendorlist = new FormGroup({
    vendorcheckId: new FormControl(''),
    documentname: new FormControl(''),
    status: new FormControl('', [Validators.required]),
    remarks: new FormControl('', [Validators.required, Validators.minLength(15)]),
    colorid: new FormControl(''),
    value: new FormControl(''),
    legalProcedings: new FormGroup({
      civilProceedingsList: new FormArray([]),
      criminalProceedingsList: new FormArray([])
    })
  });

  get civilProceedingsList() {
    return (this.vendorlist.get('legalProcedings.civilProceedingsList') as FormArray).controls;

  }

  get criminalProceedingsList() {
    return (this.vendorlist.get('legalProcedings.criminalProceedingsList') as FormArray).controls;

  }


  addCivilProceeding() {
    const civilProceedingsArray = this.vendorlist.get('legalProcedings.civilProceedingsList') as FormArray;

    civilProceedingsArray.push(this.createProceedingFormGroup('High Court ', 'All High Courts of India', 'All High Courts of India '));
    civilProceedingsArray.push(this.createProceedingFormGroup('Civil Court', 'All Civil Court', 'All Civil Court '));

    this.civilProceedingsCount++;


    console.log(this.vendorlist.value);

  }

  addCriminalProceeding() {
    const criminalProceedingsArray = this.vendorlist.get('legalProcedings.criminalProceedingsList') as FormArray;


    criminalProceedingsArray.push(this.createCriminalProceedingFormGroup('Session Court', 'All Session Courts', 'All Session Courts'));
    criminalProceedingsArray.push(this.createCriminalProceedingFormGroup('Magistrate Court', 'All Magistrate Courts', 'All Magistrate courts'));

    this.civilProceedingsCount++;


    console.log(this.vendorlist.value);

  }

  createProceedingFormGroup(staticCourt: string, staticJurisdiction: string, staticNameOfTheCourt: string): FormGroup {
    return new FormGroup({
      dateOfSearch: new FormControl('', [Validators.required]),
      court: new FormControl(staticCourt, [Validators.required]),
      jurisdiction: new FormControl(staticJurisdiction, [Validators.required]),
      nameOfTheCourt: new FormControl(staticNameOfTheCourt, [Validators.required]),
      result: new FormControl('', [Validators.required])
    });

  }


  createCriminalProceedingFormGroup(staticCourt: string, staticJurisdiction: string, staticNameOfTheCourt: string): FormGroup {
    return new FormGroup({
      dateOfSearch: new FormControl('', [Validators.required]),
      court: new FormControl(staticCourt, [Validators.required]),
      jurisdiction: new FormControl(staticJurisdiction, [Validators.required]),
      nameOfTheCourt: new FormControl(staticNameOfTheCourt, [Validators.required]),
      result: new FormControl('', [Validators.required])
    });

  }

  // utilizationReportFilter = new FormGroup({
  //   fromDate: new FormControl('', Validators.required),
  //   toDate: new FormControl('', Validators.required),
  //   // sourceId: new FormControl('', Validators.required)
  // });
  remarksModified: boolean = false;

  patchUserValues() {
    this.vendorlist.patchValue({
      colorid: 2,
    });

  }


  private updateLegalProcedings() {


    if (this.isVendorAttributeForm) {
      // @ts-ignore
      this.vendorlist.get('legalProcedings').enable();
      // @ts-ignore
      this.vendorlist.get('value').disable();
    } else {
      // @ts-ignore
      this.vendorlist.get('legalProcedings').disable();
      // @ts-ignore
      this.vendorlist.get('value').enable();
    }
    if (this.selectedStatus == '3') {
      // @ts-ignore
      this.vendorlist.get('legalProcedings').disable();
      // @ts-ignore
      this.vendorlist.get('value').disable();
    }
  }


  vendorCheckStatus: any = []
  modeOfVerificationStatus: any = [];
  initToday: any;

  constructor(private candidateService: CandidateService, public authService: AuthenticationService, calendar: NgbCalendar, private customers: CustomerService, private _router: Router, private modalService: NgbModal, private loaderService: LoaderService, private orgadmin:OrgadminDashboardService, private zone: NgZone, private customer: CustomerService) {
    // this.vendorlist = this.buildForm();
    this.orgID = this.authService.getuserId();
    this.getToday = calendar.getToday();
    console.log(this.orgID)
    // if(localStorage.getItem('dbFromDate')==null && localStorage.getItem('dbToDate')==null){
    let inityear = this.getToday.year;
    let initmonth = this.getToday.month <= 9 ? '0' + this.getToday.month : this.getToday.month;
    ;
    let initday = this.getToday.day <= 9 ? '0' + this.getToday.day : this.getToday.day;
    let initfinalDate = initday + "/" + initmonth + "/" + inityear;
    this.initToday = initfinalDate;
    // this.customers.setFromDate(this.initToday);
    // this.customers.setToDate(this.initToday);
    this.fromDate = this.initToday;
    this.toDate = this.initToday;
    // }
    var checkfromDate: any = localStorage.getItem('dbFromDate');
    let getfromDate = checkfromDate.split('/');
    this.setfromDate = { day: +getfromDate[0], month: +getfromDate[1], year: +getfromDate[2] };

    var checktoDate: any = localStorage.getItem('dbToDate');
    let gettoDate = checktoDate.split('/');
    this.settoDate = { day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2] };
    this.getMinDate = { day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2] };
    // this.utilizationReportFilter.patchValue({
    //   fromDate: this.setfromDate,
    //   toDate: this.settoDate
    // });
    this.customers.getAllVendorCheckStatus().subscribe(
      (data: any) => {
        this.vendorCheckStatus = data.data;
      }
    )
    this.getUploadVendorCheckData();

    // this.customers.getallVendorCheckDetails(this.orgID).subscribe((data: any) => {
    //   console.log(data)
    //   this.vendorchecksupload = data.data;
    //
    //   let getfromDate = data.data.fromDate.split('/');
    //   this.setfromDate = {day: +getfromDate[0], month: +getfromDate[1], year: +getfromDate[2]};
    //   this.getMinDate = this.setfromDate;
    //
    //   let gettoDate = data.data.toDate.split('/');
    //   this.settoDate = {day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2]};
    //   console.log("getfromDate, gettoDate", this.getMinDate, this.settoDate, this.fromDate, this.toDate);
    //
    //   this.start_date = 'No Date Filter';//data.data.fromDate!=null?data.data.fromDate.split('-').join('/'):''
    //   this.end_date = 'No Date Filter';//data.data.toDate!=null?data.data.toDate.split('-').join('/'):''
    //
    //   console.log("vendorchecksupload nenw fdasfdsa", this.vendorchecksupload);
    //
    // })


    // this.customers.getallVendorCheckDetailsByDateRange()
    this.candidateService.getColors().subscribe((data: any) => {
      this.getColors = data.data;
      console.log(this.getColors);
    });

    this.getReportDeliveryStatCodes = this.orgadmin.getReportDeliveryStatCode();
    if(this.getReportDeliveryStatCodes){
      var userId:any = localStorage.getItem('userId');
      var fromDate:any = localStorage.getItem('dbFromDate');
      var toDate:any = localStorage.getItem('dbToDate');
      let filterData = {
        'userId': userId,
        'fromDate': fromDate,
        'toDate': toDate,
        'status': this.getReportDeliveryStatCodes
      }
      this.orgadmin.getChartDetails(filterData).subscribe((data: any)=>{
        this.CharReportDelivery=data.data.candidateDtoList;
        console.log("Before : ", this.CharReportDelivery)
        for(let i=0; i<this.CharReportDelivery.length; i++) {
          // let index = _.findIndex(this.CharReportDelivery[i].contentDTOList, {contentSubCategory: 'PRE_APPROVAL'});
          // this.CharReportDelivery[i].pre_approval_content_id = (index != -1) ? this.CharReportDelivery[i].contentDTOList[index].contentId : -1;
          // console.log('Lavanyapre',index)
          let final = this.CharReportDelivery[i].contentDTOList;
          for (let i=0; i<final.length; i++){
            if(final[i].contentSubCategory=="FINAL"){
              this.finalreport = true
              console.log('Lavanyafinal',final[i].contentSubCategory)
            }
          }

        }


        console.log("After : ", this.CharReportDelivery)
        //console.log(data);
      });

    }
  }

  ngOnInit(): void {
    this.customers.getUserById().subscribe((data: any) => {
      this.formMyProfile = data.data
      if (data.data.createdOn) {
        this.createdOnDate = this.Dateformatter(data.data.createdOn);
      }
    });

    this.addCivilProceeding();
    this.addCriminalProceeding();


    const isCBadminVal = localStorage.getItem('roles');
    if(this.getReportDeliveryStatCodes){
      if(this.getReportDeliveryStatCodes === "PENDINGAPPROVAL"){
        $(".dbtabheading").text("QC Pending");
        this.stat_linkAdminApproval = true;
        this.stat_linkCandidateReport = false;
      }else if(this.getReportDeliveryStatCodes === "INTERIMREPORT"){
        $(".dbtabheading").text("Interim Report");
        this.stat_linkAdminApproval = false;
        this.stat_linkCandidateReport = true;
      }else if(this.getReportDeliveryStatCodes === "FINALREPORT"){
        $(".dbtabheading").text("Final Report");
        this.stat_linkAdminApproval = false;
        this.stat_linkCandidateReport = true;
      }else if(this.getReportDeliveryStatCodes === "PROCESSDECLINED"){
        $(".dbtabheading").text("Process Declined");
        this.stat_linkAdminApproval = false;
        this.stat_linkCandidateReport = false;

      }
      this.containerStat = true;
      //isCBadmin required for drilldown dashboard at Superadmin
      if(isCBadminVal == '"ROLE_CBADMIN"'){
        this.isCBadmin = true;
        this.stat_linkAdminApproval = false;
      }else{
        this.isCBadmin = false;
      }
    }
  }

  Dateformatter(timestamp: number): NgbDateStruct {
    const date = new Date(timestamp);
    return {
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate(),
    };
  }

  get filteredVendorChecks() {
    return this.vendorchecksupload.filter((item: any) =>
      item.candidate.candidateName.toLowerCase().includes(this.candidateNameFilter.toLowerCase())
    );
  }

  onfromDate(event: any) {
    let year = event.year;
    let month = event.month <= 9 ? '0' + event.month : event.month;
    ;
    let day = event.day <= 9 ? '0' + event.day : event.day;
    let finalDate = day + "/" + month + "/" + year;
    this.fromDate = finalDate;

    this.getMinDate = { day: +day, month: +month, year: +year };
  }

  ontoDate(event: any) {

    let year = event.year;
    let month = event.month <= 9 ? '0' + event.month : event.month;
    ;
    let day = event.day <= 9 ? '0' + event.day : event.day;
    ;
    let finalDate = day + "/" + month + "/" + year;
    this.toDate = finalDate;


  }

  get remarksControl() {
    return this.vendorlist.get('remarks') as FormControl;
  }

  isValidDateRange(value: string): boolean {
    // Define the regex pattern for dd-mm-yyyy - dd-mm-yyyy
    const pattern = /^\d{2}-\d{2}-\d{4}\s*to\s*\d{2}-\d{2}-\d{4}$/;
    return pattern.test(value);
  }

  testDateRange(value: string) {
    const pattern = /^\d{2}-\d{2}-\d{4}$/;
    return pattern.test(value);

  }

  getUploadVendorCheckData() {
    this.filteredData = [];
    // this.customers.setFromDate(this.fromDate);
    // this.customers.setToDate(this.toDate);
    let filterData = {
      userId: this.orgID,
      fromDate: this.customers.getFromDate(),
      toDate: this.customers.getToDate(),
      pageNumber: this.pageNumber,
      status: this.orgadmin.getReportDeliveryStatCode()
    };
    this.customers.getallVendorCheckDetailsByDateRange(filterData).subscribe((data: any) => {
      if (data.outcome === true) {
        this.vendorchecksupload = data.data;
        this.totalPages = Number(data.message);
        this.filteredData = this.vendorchecksupload;
      }
      const startIndex = this.currentPageIndex * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      return this.filteredData;
    });
  }

  applyFilter() {
    const filterText = this.candidateNameFilter.toLowerCase();
    if (filterText === '') {
      this.filteredData = this.vendorchecksupload;
    } else {
      this.filteredData = this.vendorchecksupload.filter((item: any) =>
        item.candidate.candidateName.toLowerCase().includes(filterText)
      );
    }
  }

  // onSubmitFilter() {
  //
  //   this.fromDate = this.fromDate != null ? this.fromDate : '';
  //   this.toDate = this.toDate != null ? this.toDate : '';
  //   this.utilizationReportFilter.patchValue({
  //     fromDate: this.fromDate,
  //     toDate: this.toDate,
  //   });
  //   localStorage.setItem('dbFromDate', this.fromDate);
  //   localStorage.setItem('dbToDate', this.toDate);
  //   let filterData = {
  //     userId: this.orgID,
  //     fromDate: localStorage.getItem("dbFromDate"),
  //     toDate: localStorage.getItem("dbToDate"),
  //   };
  //
  //   console.log("filtered data  :" + filterData)
  //   this.customers.getallVendorCheckDetailsByDateRange(filterData).subscribe((data: any) => {
  //     if (data.outcome === true) {
  //       this.vendorchecksupload = data.data;
  //       console.log("this.vendorchecksupload", this.vendorchecksupload);
  //       // this.start_date = data.data.fromDate != null ? data.data.fromDate.split('-').join('/') : ''
  //       // this.end_date = data.data.toDate != null ? data.data.toDate.split('-').join('/') : ''
  //     }
  //   });
  // }
  onSubmitFilter() {
    this.candidateNameFilter = '';
    const inputFromDate: any = $("#inputFromDateVendor").val();
    const finalInputFromDate = inputFromDate;
    const inputToDate: any = $("#inputToDateVendor").val();
    const finalInputToDate = inputToDate;
    if (this.fromDate == null) {
      this.fromDate = finalInputFromDate;
    }
    if (this.toDate == null) {
      this.toDate = finalInputToDate;
    }
    // if (this.utilizationReportFilter.valid) {
    //   this.getUploadVendorCheckData();
    // }
  }


  // uploadGlobalCase(event: any) {
  //   const file = event.target.files[0];
  //   const reader = new FileReader();
  //   reader.onload = function (e) {
  //     const data = new Uint8Array(e.target.result);
  //     const xhr = new XMLHttpRequest();
  //     xhr.open('POST', '/upload', true);
  //     xhr.setRequestHeader('Content-Type', 'application/octet-stream');
  //     xhr.send(data);
  //   };
  //   reader.readAsArrayBuffer(file);
  // }

  uploadGlobalCaseDetails(files: FileList | null) {
    if (!files || files.length === 0) {
      return;
    }

    const file = files[0];
    const fileType = file.name.split('.').pop();

    if (fileType) {
      const lowerCaseFileType = fileType.toLowerCase();

      if (
        lowerCaseFileType === 'pdf' ||
        lowerCaseFileType === 'png' ||
        lowerCaseFileType === 'jpg' ||
        lowerCaseFileType === 'jpeg' ||
        lowerCaseFileType === ' '
      ) {
        this.proofDocumentNew = file;
        this.previewFile(file);
      } else {
        this.isButtonDisabled=true;
        Swal.fire({
          title: 'Please select .jpeg, .jpg, .png, or .pdf file type only.',
          icon: 'warning'
        });
      }
    } else {
      // Handle the case where fileType is an empty string
      this.proofDocumentNew=null;
      Swal.fire({
        title: 'Please select a file.',
        icon: 'warning'
      });
    }

  }

  switchForm(form: string) {
    this.activeForm = form;
  }

  previewFile(file: File) {
    const previewContainer = document.getElementById('preview-container');
    const lowerCaseFileType = file.type;

    if (previewContainer) {
      if (
        lowerCaseFileType === 'application/pdf' ||
        lowerCaseFileType === 'image/png' ||
        lowerCaseFileType === 'image/jpeg'||
        lowerCaseFileType === ' ')
      {
        const previewButton = document.createElement('button');
        previewButton.textContent = 'Preview';
        previewButton.addEventListener('click', () => {
          this.previewFile(file);
        });
        const downloadLink = document.createElement('a');
        downloadLink.href = URL.createObjectURL(file);
        downloadLink.target = '_blank';
        downloadLink.textContent = 'Preview';
        downloadLink.classList.add('btn', 'btn-primary');

        previewContainer.innerHTML = '';
        previewContainer.appendChild(downloadLink);
      } else {
        previewContainer.innerHTML = 'Preview not available for this file type.';
      }
    }
  }

  openCertificate(modalCertificate: any, certificate: any) {
    this.modalService.open(modalCertificate, {
      centered: true,
      backdrop: 'static',
      size: 'lg'
    });
    var maxFileSize = 1000000; // 1MB
    if (certificate && certificate.length <= maxFileSize) {
      this.loadCertificatePDF(certificate);
    }
  }

  loadCertificatePDF(certificate: any) {
    const pdfUrl = 'data:application/pdf;base64,' + certificate;
    const iframe = document.getElementById('viewcandidateCertificate') as HTMLIFrameElement;
    iframe.src = pdfUrl;
  }

  getcolor(event: any) {
    console.log(event.target.value)
    this.colorid = event.target.value
  }

  getModeOfVerificationPerformed(value: any) {

  }

  // patchAddIdValues() {
  //   this.vendorlist.patchValue({
  //     candidateId: this.candidateId,
  //     sourceId: this.sourceId,
  //     vendorId: this.vendorId,
  //   });
  // }
  isButtonDisabled: boolean = false;

  getRemarks(): any {
    return this.vendorlist.get('remarks');
  }

  getValue(): any {
    return this.vendorlist.get('value');
  }

  controlTouched: boolean[] = [];
  selectedStatus: any;

  markControlAsTouched(index: number) {
    this.controlTouched[index] = true;
  }

  isControlTouched(index: number) {
    return this.controlTouched[index];
  }

  async addCandidateData(vendorData: any, triggerRequestId: any): Promise<any> {
    return new Promise<any>((resolve, reject) => { // Change the Promise type to any
      this.loaderService.show();
      this.customers.saveSubmittedCandidatesForTriggerCheckStatus(vendorData, triggerRequestId).subscribe(
        (data: any) => {
          if (data.outcome === true) {
            Swal.fire({
              title: data.message,
              icon: 'warning'
            }).then((result) => {
              if (result.isConfirmed) {
                window.location.reload();
              }
            });
            resolve(data); // Resolve with the data
          } else {
            reject(data); // Reject with the data in case of an error
          }
        },
        (error) => {
          reject(error); // Reject with the error
        }
      );
    });
  }

  @ViewChild('insuffmodalsecoundremarks') insuffmodalsecoundremarks: ElementRef | undefined;
  apicomplete: any = false;

  getCurrentStatusOfCheck(item: any) {
    this.apicomplete = false;
    let vendorData = {
      VendorID: "2CDC7E3A"
    }
    console.log('----------------------candidate fetch starts-------------------------');
    this.addCandidateData(vendorData, item.candidate.conventionalRequestId)
      .then((data: any) => {
        console.log(data.outcome + "outcome");
      })
      .catch((error: any) => {
        console.log(error.outcome + "error")
        if (error.outcome === false) {
          this.triggerInsufficiencySecoundRemarksModal(this.insuffmodalsecoundremarks, item);
        }
      })
      .finally(() => {
        this.loaderService.hide();
      });
  }


  // triggerModal(content: any, item: any) {
  //   this.vendorlist.reset();
  //   // this.isVendorAttributeForm = item.checkname.includes('CRIMINAL CHECK');
  //   this.customers.getVendorReportAttributes(item.vendorcheckId).subscribe((response: any) => {
  //     const vendorData = response.data;
  //     this.venderAttributeValue = vendorData.map((vendor: any) => {
  //       return vendor.checkAttibutes.map((attr: any) => {
  //         return {
  //           label: attr,
  //           value: ""
  //         };
  //       });
  //     }).flat();
  //   });
  triggerModal(content: any, item: any) {

    this.isVendorAttributeForm = item.source?.sourceName.includes('CRIMINAL CHECK');

    this.globaldataBaseCheck = item.source?.sourceName.includes('GLOBAL DATABASE CHECK');
    if (this.globaldataBaseCheck) {
      if (this.globalCheckType != null) {
        this.globalCheckType = "INDIA";
        this.customers.getVendorReportAttributes(item.vendorcheckId, this.globalCheckType).subscribe(data => {
          // @ts-ignore
          this.globalAttributeValue = data.data.checkAttibutes.map((attr: any) => {
            return {
              label: attr,
              value: "No Records Found"
            };
          });
          console.log("globalAttributeValue :" + JSON.stringify(this.globalAttributeValue))
        });
      }
      if (this.globalCheckType != null) {
        this.globalCheckType = "GLOBAL";
        this.customers.getVendorReportAttributes(item.vendorcheckId, this.globalCheckType).subscribe(data => {
          // @ts-ignore
          this.indiaAttributeValue = data.data.checkAttibutes.map((attr: any) => {
            return {
              label: attr,
              value: "No Records Found"
            };
          });
          console.log("indiaAttributeValue :" + JSON.stringify(this.indiaAttributeValue))
        });
      }
    }
    this.updateLegalProcedings();
    if (this.globaldataBaseCheck === false) {
      this.customers.getVendorReportAttributes(item.vendorcheckId, this.globalCheckType).subscribe(data => {
        // @ts-ignore
        this.venderAttributeValue = data.data.checkAttibutes.map((attr: any) => {
          return {
            label: attr,
            value: ""
          };
        });
      });
    }

    this.modalService.open(content).result.then((res) => {
      console.log(content, "........................");
      this.closeModal = `Closed with: ${res}`;
    }, (res) => {
      this.closeModal = `Dismissed ${this.getDismissReason(res)}`;
    });

    console.log(item.documentname, "........................");
    this.vendorlist.patchValue({
      documentname: item.documentname,
      status: status,
      vendorcheckId: item.vendorcheckId,
      colorid: this.colorid,
    });
  }

  getVendorStatusID(vendorCheckStatusID: any) {
    // alert("fdsafdas"+vendorCheckStatusID)
  }

  updateLiCheckStatus(status: any, vendorCheckId: any, remarks: any, modeOfVerificationStatus: any) {
    console.log("updating licheckdata")
    this.customers.updateLiCheckStatusByVendorID(status, vendorCheckId, remarks, modeOfVerificationStatus).subscribe(data => {
      console.log(data)
    });

  }

  // udpateBgvStatusRowWise(data: any) {
  //   this.customers.updateBgvCheckStatusRowWise(data).subscribe(data => {
  //     console.log(data);
  //   })
  // }

  isUploadButtonDisabled(item: any): boolean {
    // [disabled]="isUploadButtonDisabled(item)"
    // || item.vendorCheckStatusMaster?.checkStatusCode === 'INSUFFICIENCY'
    return item.vendorCheckStatusMaster?.checkStatusCode === 'CLEAR';
  }

  onSubmit(vendorlist: FormGroup) {
    this.isButtonDisabled = true
    let rawValue = vendorlist.getRawValue();
    // console.log(this.globalAttributeValue)
    // @ts-ignore
    this.selectedGlobalAttributeValue.push(...this.globalAttributeValue);
    console.log("global1" + JSON.stringify(this.selectedGlobalAttributeValue))
    this.attributeMap.set('GLOBAL', this.selectedGlobalAttributeValue);
    console.log(JSON.stringify(this.selectedIndiaAttributeValue))
    this.selectedIndiaAttributeValue.push(...this.indiaAttributeValue);
    this.attributeMap.set('INDIA', this.selectedIndiaAttributeValue);
    console.log(JSON.stringify(this.attributeMap))
    console.log('Attribute Map:', JSON.stringify(Array.from(this.attributeMap.entries())));
    this.patchUserValues();
    console.log(this.vendorlist.value, "----------------------------------------")
    const formData = new FormData();
    if (this.globaldataBaseCheck) {
      const mergedData = {
        ...this.venderAttributeCheckMapped,
      };
      const attributeMapObject: { [key: string]: any[] } = {};
      for (const [key, value] of this.attributeMap.entries()) {
        attributeMapObject[key] = value;
      }
      if(rawValue.status==='3'){
        // @ts-ignore
        formData.append('vendorRemarksReport', null);
      }
      else {
        formData.append('vendorRemarksReport', JSON.stringify(attributeMapObject));
      }
    } else {
      const venderAttributeValuesGloble = this.venderAttributeValue.reduce((obj, item) => {
        obj[item.label] = item.value;
        return obj;
      }, {});
      // delete agentAttributeValues.value
      this.venderAttributeCheckMapped = { ...venderAttributeValuesGloble }
      console.log(" onSubmit:::", this.venderAttributeCheckMapped);
      console.warn("venderAttributeValues===>", venderAttributeValuesGloble);
      const mergedData = {
        ...this.venderAttributeCheckMapped,
      };
      if(rawValue.status==='3'){
        // @ts-ignore
        formData.append('vendorRemarksReport', null);
      }else{
      formData.append('vendorRemarksReport', JSON.stringify(mergedData));
      }
    }
    formData.append('file', this.proofDocumentNew);
    formData.append('vendorchecks', JSON.stringify(this.vendorlist.value));
    // formData.append('vendorRemarksReport', JSON.stringify(mergedData));
    // this.updateLiCheckStatus(rawValue.status, rawValue.vendorcheckId, rawValue.remarks, rawValue.modeofverificationperformed);
    return this.customers.saveproofuploadVendorChecks(formData).subscribe((result: any) => {
      if (result.outcome === true) {
        Swal.fire({
          title: result.message,
          icon: 'success'
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.reload();
          }
        });
      } else {
        Swal.fire({
          title: result.message,
          icon: 'warning'
        })
      }
    });
    // }
    this.isButtonDisabled = false;
  }


  private getDismissReason(reason: any): string {
    window.location.reload();
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

  getvendorid(id: any) {
    this.getvendorid = id;
    let agentIdsArray: any = [];
    agentIdsArray.push(id);
    this.getvendorid = agentIdsArray;

  }

  dashboardRedirect(id: any) {
    this.customers.getVendorList(Number(id)).subscribe((result: any) => {
      console.log(result);
      if (result.outcome === true) {
        localStorage.setItem('orgID', id);
        localStorage.setItem('userId', result.data.userId);
        this._router.navigate(['admin/orgadminDashboard']);
      } else {
        Swal.fire({
          title: result.message,
          icon: 'warning'
        })
      }
    });
  }

  downloadReferenceExcelData(candidateName: any, sourceName: any, candidateId: any, sourceId: any) {
    this.candidateService.generateReferenceDataForVendor(candidateId, sourceId).subscribe((data: any) => {
      const link = document.createElement('a');
      link.href = 'data:application/vnd.ms-excel;base64,' + data.message;
      // @ts-ignore
      // link.href = 'data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,' + encodeURIComponent(data.message);

      link.download = candidateName + "_" + sourceName + ".xlsx";
      link.target = '_blank';
      link.click();
    });
  }

  performSearch() {
    this.candidateNameFilter = this.candidateNameFilter.toLowerCase();
    if (this.candidateNameFilter === "") {
      this.getUploadVendorCheckData();
    }
    console.log('Search Text:', this.candidateNameFilter);
    this.customers.getAllVendorSearch(this.candidateNameFilter).subscribe((data: any) => {
      this.filteredData = data.data;
      console.log("", data);
    })
  }

  downloadPdf(agentUploadedDocument: any) {
    console.log(agentUploadedDocument, "******************************");
    if (agentUploadedDocument == null || agentUploadedDocument == "") {
      console.log("No Document Found")
    }

    this.customers.generatePrecisedUrl(agentUploadedDocument).subscribe(data => {

      // specify the URL of the file
      // @ts-ignore
      // let precisedurl = data.data;
      //
      // var parts = precisedurl.split('.'); // split the URL by the "." character
      // var extension = parts[parts.length - 1]; // get the last part of the URL, which should be the file extension
      //
      // if (extension !== 'pdf') {
      //   precisedurl = precisedurl.replace('.' + extension, '.png'); // replace the extension with ".png"
      // }
      //
      // window.open(precisedurl, "_blank", "resizable=yes,scrollbars=yes,status=yes");

      //
      // // @ts-ignore
      // var mime_type = "image/png"; // specify the MIME type of the file
      // @ts-ignore
      window.open(data.data);

      // var mime_type = "image/png"; // specify the MIME type of the PNG image
      // //@ts-ignore
      // window.open(data.data).document.type = mime_type;

    })
    // if (agentUploadedDocument != null) {
    //   const linkSource = 'data:application;base64,' + agentUploadedDocument;
    //   const downloadLink = document.createElement("a");
    //   downloadLink.href = linkSource;
    //   downloadLink.download = "Download.pdf"
    //   downloadLink.click();
    // } else {
    //   Swal.fire({
    //     title: 'No Documents Uploaded',
    //     icon: 'warning'
    //   })
    // }
  }

  secoundaryRemarks: any;
  tempSecoundaryRemarks: string = '';


  triggerInsufficiencySecoundRemarksModal(content: any, item: any) {
    this.customers.getRemarksByCheckUniqueId(item.checkUniqueId).subscribe((data: any) => {

      this.secoundaryRemarks = data.data;
      this.tempSecoundaryRemarks = data.data;
      this.remarksModified = false;
    })
    this.modalService.open(content).result.then((res) => {
      console.log(content, "........................")
      this.closeModal = `Closed with: ${res}`;
    }, (res) => {
      this.closeModal = `Dismissed ${this.getDismissReason(res)}`;
    });
    this.vendorlist.patchValue({
      documentname: item.documentname,
      status: item.vendorCheckStatusMaster?.vendorCheckStatusMasterId,
      vendorcheckId: item.vendorcheckId,
      colorid: 2,
    });
  }

  onSecoundaryRemarksChange() {
    this.remarksModified = this.secoundaryRemarks !== this.tempSecoundaryRemarks;
  }

  saveRemarks() {
    const formData = new FormData();
    this.tempSecoundaryRemarks = this.secoundaryRemarks;
    this.vendorlist.patchValue({
      remarks: this.tempSecoundaryRemarks
    })
    console.log(this.vendorlist.getRawValue())
    formData.append('vendorchecks', JSON.stringify(this.vendorlist.value));

    return this.customers.updateBgvCheckStatusRowWise(formData).subscribe((result: any) => {
      if (result.outcome === true) {
        Swal.fire({
          title: result.message,
          icon: 'success'
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.reload();
          }
        });
      } else {
        Swal.fire({
          title: result.message,
          icon: 'warning'
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.reload();
          }
        });
      }
    });
  }

  // get totalPages(): number {
  //   const filteredItems = this.filteredData;
  //   return Math.ceil(filteredItems.length / this.pageSize);
  // }

  goToPrevPage(): void {
    // this.idvalue=idvalue;
    if (this.currentPageIndex > 0) {
      this.currentPageIndex--;
    }
    this.pageNumber--;
    this.getUploadVendorCheckData();
  }

  goToNextPage(): void {
    if (this.currentPageIndex < this.totalPages - 1) {
      this.currentPageIndex++;
    }
    this.pageNumber++;
    this.getUploadVendorCheckData();
  }

  filteredDatapagination(): any[] {
    let filteredItems = this.filteredData;
    const startIndex = this.currentPageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return filteredItems;
  }

  // filterToday() {
  //   let inityear = this.getToday.year;
  //   let initmonth =
  //     this.getToday.month <= 9
  //       ? '0' + this.getToday.month
  //       : this.getToday.month;
  //   let initday =
  //     this.getToday.day <= 9 ? '0' + this.getToday.day : this.getToday.day;
  //   let initfinalDate = initday + '/' + initmonth + '/' + inityear;
  //   this.initToday = initfinalDate;
  //   this.fromDate = this.initToday;
  //   this.toDate = this.initToday;
  //   let getfromDate = this.initToday.split('/');
  //   this.setfromDate = { day: +getfromDate[0], month: +getfromDate[1], year: +getfromDate[2] };
  //   let gettoDate = this.initToday.split('/');
  //   this.settoDate = { day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2] };
  //   this.getMinDate = { day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2] };
  //   this.utilizationReportFilter.patchValue({
  //     fromDate: this.setfromDate,
  //     toDate: this.settoDate
  //   });
  //   this.getUploadVendorCheckData();
  // }

  // filterLastMonth() {
  //   let date = new Date();
  //   date.setMonth(date.getMonth() - 1);
  //   let firstDayOfMonth = new Date(date.getFullYear(), date.getMonth(), 2);
  //   let lastDayOfMonth = new Date(date.getFullYear(), date.getMonth() + 1, 1);
  //   let fromDateString = firstDayOfMonth.toISOString().split('T')[0];
  //   let toDateString = lastDayOfMonth.toISOString().split('T')[0];

  //   let getInputFromDate: any = fromDateString.split('-');
  //   let finalInputFromDate =
  //     getInputFromDate[2] +
  //     '/' +
  //     getInputFromDate[1] +
  //     '/' +
  //     getInputFromDate[0];

  //   let getInputToDate: any = toDateString.split('-');
  //   let finalInputToDate =
  //     getInputToDate[2] +
  //     '/' +
  //     getInputToDate[1] +
  //     '/' +
  //     getInputToDate[0];
  //   this.fromDate = finalInputFromDate;
  //   this.toDate = finalInputToDate;
  //   let getfromDate = finalInputFromDate.split('/');
  //   this.setfromDate = { day: +getfromDate[0], month: +getfromDate[1], year: +getfromDate[2] };
  //   let gettoDate = finalInputToDate.split('/');
  //   this.settoDate = { day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2] };
  //   this.getMinDate = { day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2] };
  //   this.utilizationReportFilter.patchValue({
  //     fromDate: this.setfromDate,
  //     toDate: this.settoDate
  //   });
  //   this.getUploadVendorCheckData();
  // }

  // filterMonthToDate() {
  //   let currentDate = new Date();
  //   let firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 2);
  //   let fromDateString = firstDayOfMonth.toISOString().split('T')[0];
  //   let toDateString = currentDate.toISOString().split('T')[0];

  //   let getInputFromDate: any = fromDateString.split('-');
  //   let finalInputFromDate =
  //     getInputFromDate[2] +
  //     '/' +
  //     getInputFromDate[1] +
  //     '/' +
  //     getInputFromDate[0];

  //   let inityear = this.getToday.year;
  //   let initmonth =
  //     this.getToday.month <= 9
  //       ? '0' + this.getToday.month
  //       : this.getToday.month;
  //   let initday =
  //     this.getToday.day <= 9 ? '0' + this.getToday.day : this.getToday.day;
  //   let initfinalDate = initday + '/' + initmonth + '/' + inityear;
  //   this.initToday = initfinalDate;
  //   this.fromDate = finalInputFromDate;
  //   this.toDate = this.initToday;
  //   let getfromDate = finalInputFromDate.split('/');
  //   this.setfromDate = { day: +getfromDate[0], month: +getfromDate[1], year: +getfromDate[2] };
  //   let gettoDate = this.initToday.split('/');
  //   this.settoDate = { day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2] };
  //   this.getMinDate = { day: +gettoDate[0], month: +gettoDate[1], year: +gettoDate[2] };
  //   this.utilizationReportFilter.patchValue({
  //     fromDate: this.setfromDate,
  //     toDate: this.settoDate
  //   });
  //   this.getUploadVendorCheckData();
  // }

  selectTab(tabName: string): void {
    this.selectedTab = tabName;
    // Add any additional logic you want to perform when a tab is selected
  }
  addSelectedIndiaAttribute(selectedIndiaAttr: any) {
    if (selectedIndiaAttr) {
      // Add the selected value to the array
      this.selectedIndiaAttributeValue.push({ label: selectedIndiaAttr, value: '' });
    }
    console.log("indian attrlist" + JSON.stringify(this.selectedIndiaAttributeValue))
  }
  isGlobalValueSelected(label: string): boolean {
    // Check if the value is already selected
    return this.selectedGlobalAttributeValue.some(attribute => attribute.label === label);
  }
  isIndianValueSelected(label: string): boolean {
    // Check if the value is already selected
    return this.selectedIndiaAttributeValue.some(attribute => attribute.label === label);
  }
  addSelectedGlobalAttribute(value: string) {
    const selectedAttribute = this.globalAttributeValue.find(attr => attr.label === value);

    if (selectedAttribute) {
      // Remove the selected attribute from the globalAttributeValue array
      const index = this.globalAttributeValue.indexOf(selectedAttribute);
      if (index !== -1) {
        this.globalAttributeValue.splice(index, 1);
      }
    }
    this.selectedGlobalAttributeValue.push({ label: value, value: '' });
  }
  addSelectedIndianAttribute(value: string) {
    const selectedIndAttribute = this.indiaAttributeValue.find(attr => attr.label === value);

    if (selectedIndAttribute) {
      // Remove the selected attribute from the globalAttributeValue array
      const index = this.indiaAttributeValue.indexOf(selectedIndAttribute);
      if (index !== -1) {
        this.indiaAttributeValue.splice(index, 1);
      }
    }
    this.selectedIndiaAttributeValue.push({ label: value, value: '' });
  }
  onKeyUpGlobal(value: string, label: string): void {
    debugger
    // Find the attribute in the array based on the label
    const foundGlobalAttribute = this.selectedGlobalAttributeValue.find(attr => attr.label === label);
    // Update the value if the attribute is found
    if (foundGlobalAttribute) {
      foundGlobalAttribute.value = value;
    }
    console.log(JSON.stringify(foundGlobalAttribute))
  }
  onKeyUpIndian(value: string, label: string): void {
    debugger
    // Find the attribute in the array based on the label
    const foundIndianAttribute = this.selectedIndiaAttributeValue.find(attr => attr.label === label);
    // Update the value if the attribute is found
    if (foundIndianAttribute) {
      foundIndianAttribute.value = value;
    }
    console.log(JSON.stringify(foundIndianAttribute))
  }
  // In your component class
  disableIndianOptionButton: any;
  disableGlobalOptionButton: any;
  isOptionSelectedIndian(label: string): boolean {
    this.disableIndianOptionButton = this.selectedIndiaAttributeValue.some(attribute => attribute.label === label);
    return this.disableIndianOptionButton
  }
  isOptionSelectedGlobal(label: string): boolean {
    this.disableGlobalOptionButton = this.selectedGlobalAttributeValue.some(attribute => attribute.label === label);
    return this.disableGlobalOptionButton;
  }


  ngAfterViewInit() {
    setTimeout(() =>{
      this.ngOnDestroy();
      this.loadCharts();
    },50);
  }


  loadCharts(){
    this.zone.runOutsideAngular(() => {
      let chart = am4core.create("chartReportDelivery", am4charts.PieChart);
      chart.innerRadius = am4core.percent(50);
      chart.legend = new am4charts.Legend();

      chart.legend.itemContainers.template.paddingTop = 4;
      chart.legend.itemContainers.template.paddingBottom = 4;
      chart.legend.fontSize = 13;
      chart.legend.useDefaultMarker = true;
      let marker:any = chart.legend.markers.template.children.getIndex(0);
      marker.cornerRadius(12, 12, 12, 12);
      marker.strokeWidth = 3;
      marker.strokeOpacity = 1;
      marker.stroke = am4core.color("#000");

      chart.legend.maxHeight = 210;
      chart.legend.scrollable = true;
      chart.legend.position = "right";
      chart.logo.disabled = true;
      chart.padding(10, 0, 0, 0);
      chart.radius = am4core.percent(95);
      chart.paddingRight = 0;

      // Add and configure Series
      let pieSeries = chart.series.push(new am4charts.PieSeries());
      pieSeries.slices.template.stroke = am4core.color("#fff0");
      pieSeries.slices.template.strokeWidth = 0;
      pieSeries.slices.template.strokeOpacity = 0;

      pieSeries.slices.template.tooltipText = "{category}: {value}";
      pieSeries.labels.template.disabled = true;
      pieSeries.dataFields.value = "value";
      pieSeries.dataFields.category = "name";0

      // This creates initial animation
      pieSeries.hiddenState.properties.opacity = 1;
      pieSeries.hiddenState.properties.endAngle = -90;
      pieSeries.hiddenState.properties.startAngle = -90;
      pieSeries.legendSettings.itemValueText = "[bold]{value}[/bold]";
      pieSeries.colors.list = [
        am4core.color("#FF8E00"),
        am4core.color("#ffd400"),
        am4core.color("#fd352c"),
        am4core.color("#08e702"),
        am4core.color("#9c27b0"),
        am4core.color("#021aee"),
      ];

      // var rgm = new am4core.RadialGradientModifier();
      // rgm.brightnesses.push(-0.2, -0.2, -0.1, 0, - 0.1);
      // pieSeries.slices.template.fillModifier = rgm;
      // pieSeries.slices.template.strokeModifier = rgm;
      // pieSeries.slices.template.strokeWidth = 0;


      //pieSeries.slices.template.events.on("hit", myFunction, this);
      pieSeries.slices.template.events.on('hit', (e) => {
        const getchartData = e.target._dataItem as any;
        const statuscodes = getchartData._dataContext.statcode;
        //console.log(statuscodes);
        this.orgadmin.setReportDeliveryStatCode(statuscodes);
        window.location.reload();
      });
      chart.legend.itemContainers.template.events.on("hit", (ev) => {
        const getchartData = ev.target._dataItem as any;
        const statuscodes = getchartData._label._dataItem._dataContext.statcode;
        this.orgadmin.setReportDeliveryStatCode(statuscodes);
        window.location.reload();
      });
      pieSeries.slices.template.cursorOverStyle = am4core.MouseCursorStyle.pointer;
    
      var userId:any = localStorage.getItem('userId');
      var fromDate:any = localStorage.getItem('dbFromDate');
      var toDate:any = localStorage.getItem('dbToDate');
      let filterData = {
        'userId': userId,
        'fromDate': fromDate,
        'toDate': toDate
      }

      console.log(filterData)
      this.orgadmin.getReportDeliveryDetails(filterData).subscribe((uploadinfo: any)=>{
        this.CharReportDeliveryData=uploadinfo.data.candidateStatusCountDto;
        //console.log(this.CharReportDeliveryData);
        let data = [];
        for (let i = 0; i < this.CharReportDeliveryData.length; i++) {
          data.push({name: this.CharReportDeliveryData[i].statusName, value: this.CharReportDeliveryData[i].count, statcode: this.CharReportDeliveryData[i].statusCode });
        }
        chart.data = data;
      });
    });
  }

ngOnDestroy() {
  this.zone.runOutsideAngular(() => {
    if (this.chart) {
      this.chart.dispose();
    }
  });
}
}

