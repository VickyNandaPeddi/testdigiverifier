import { Component, OnInit } from '@angular/core';
import { OrgadminService } from 'src/app/services/orgadmin.service';
import {ModalDismissReasons, NgbModal, NgbCalendar, NgbDate} from '@ng-bootstrap/ng-bootstrap';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import { HttpEventType, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import Swal from 'sweetalert2';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { OrgadminDashboardService } from 'src/app/services/orgadmin-dashboard.service';
import { LoaderService } from 'src/app/services/loader.service';
import { CustomerService } from 'src/app/services/customer.service';
@Component({
  selector: 'app-orgadmin-dashboard',
  templateUrl: './orgadmin-dashboard.component.html',
  styleUrls: ['./orgadmin-dashboard.component.scss']
})
export class OrgadminDashboardComponent implements OnInit {
  pageTitle = 'Dashboard';
  closeModal: string | undefined;
  selectedFiles: any;
  currentFile: any;
  containerStat:boolean = false;
  fileInfos: any;
  getReportDeliveryStatCodes: any;
  getPendingDetailsStatCode: any;
  getStatCodes:any;
  isShowDiv:boolean=false;
  isCBadmin:boolean = false;
  getUserByOrganizationIdAndUserId:any=[];
  getRolePerMissionCodes:any=[];
  AGENTUPLOAD_stat:boolean=false;
  CANDIDATEUPLOAD_stat:boolean=false;
  fromDate:any;
  toDate:any;
  getToday: NgbDate;
  getMinDate: any;
  setfromDate:any;
  settoDate:any;
  initToday:any;
  getStatus:any=[];
  dashboardFilter = new FormGroup({
    fromDate: new FormControl('', Validators.required),
    toDate: new FormControl('', Validators.required)
  });
  SAactivityFilter:any=['NEWUPLOAD'];
  constructor(private orgadmin:OrgadminService, private modalService: NgbModal, private customers:CustomerService,
    public authService: AuthenticationService,  private dashboardservice:OrgadminDashboardService,
    public loaderService: LoaderService, public calendar: NgbCalendar, private customer: CustomerService) {
      this.getReportDeliveryStatCodes = this.dashboardservice.getReportDeliveryStatCode();
      this.getPendingDetailsStatCode = this.dashboardservice.getPendingDetailsStatCode();
      this.getStatCodes = this.dashboardservice.getStatusCode();
      this.dashboardservice.getUsersByRoleCode(this.authService.getRoles()).subscribe((data: any)=>{
        this.getUserByOrganizationIdAndUserId=data.data;
        //console.log(this.getUserByOrganizationIdAndUserId)
      });

      this.customers.getAllStatus().subscribe((data: any)=>{
        this.getStatus=data.data;
        //console.log(this.getStatus);
      })

      this.getToday = calendar.getToday();
        let inityear = this.getToday.year;
        let initmonth = this.getToday.month <= 9 ? '0' + this.getToday.month : this.getToday.month;;
        let initday = this.getToday.day <= 9 ? '0' + this.getToday.day : this.getToday.day;
        let initfinalDate = initday + "/" + initmonth + "/" + inityear;
        this.initToday = initfinalDate;
        if(localStorage.getItem('dbFromDate')==null && localStorage.getItem('dbToDate')==null){
        this.customer.setFromDate(this.initToday);
        this.customer.setToDate(this.initToday);
        this.fromDate = this.initToday;
        this.toDate = this.initToday;
      }

      var checkfromDate:any = localStorage.getItem('dbFromDate');
      let getfromDate = checkfromDate.split('/');
      this.setfromDate = { day:+getfromDate[0],month:+getfromDate[1],year:+getfromDate[2]};

      var checktoDate:any = localStorage.getItem('dbToDate');
      let gettoDate =checktoDate.split('/');
      this.settoDate = { day:+gettoDate[0],month:+gettoDate[1],year:+gettoDate[2]};
      this.getMinDate = { day:+gettoDate[0],month:+gettoDate[1],year:+gettoDate[2]};

      this.dashboardFilter.patchValue({
        fromDate: this.setfromDate,
        toDate: this.settoDate
       });


    }
  uploadAgent = new FormGroup({
    file: new FormControl('', Validators.required)
  });

  selectFile(event:any) {
    const fileType = event.target.files[0].name.split('.').pop();
    if(fileType == 'xlsx' || fileType == 'XLSX' || fileType == 'xls' || fileType == 'XLS' || fileType == 'csv' || fileType == 'CSV'){
      this.selectedFiles = event.target.files;
    }else{
      event.target.value = null;
      Swal.fire({
        title: 'Please select .xlsx, .xls, .csv file type only.',
        icon: 'warning'
      });
    }

  }

  uploadAgents() {
    this.currentFile = this.selectedFiles.item(0);
    this.orgadmin.uploadAgent(this.currentFile).subscribe(
      (event:any) => {
        //console.log(event);
        if(event instanceof HttpResponse){
          Swal.fire({
            title: event.body.message,
            icon: 'success'
          }).then(function() {
            window.location.reload();
        });
        }
       });
  }

  uploadCandidate() {
    this.currentFile = this.selectedFiles.item(0);
    this.orgadmin.uploadCandidate(this.currentFile).subscribe(
      (event:any) => {
       // console.log(event);
        if(event instanceof HttpResponse){
          if(event.body.outcome == true){
            Swal.fire({
              title: event.body.message,
              icon: 'success'
            }).then(function() {
              window.location.reload();
          });
          }
          else{
            Swal.fire({
              title: event.body.message,
              icon: 'error'
            }).then(function() {
              window.location.reload();
          });
          }
        }

       });
  }

  activityFilter(activity:any){
    this.SAactivityFilter = [];
    this.SAactivityFilter.push(activity);
  }

  uploadClientscope() {
    this.currentFile = this.selectedFiles.item(0);
    this.orgadmin.uploadClientscope(this.currentFile).subscribe(
      (event:any) => {
        //console.log(event);
        if(event instanceof HttpResponse){
          Swal.fire({
            title: event.body.message,
            icon: 'success'
          }).then(function() {
            window.location.reload();
        });
        }
       });
  }

  ngOnInit(): void {
    if(this.getPendingDetailsStatCode){
      this.isShowDiv = true;
    }else if(this.getReportDeliveryStatCodes){
      this.isShowDiv = false;
    }

    if(this.getStatCodes || this.getPendingDetailsStatCode || this.getReportDeliveryStatCodes){
      this.containerStat = true;
    }
    setTimeout(() =>{
      this.loaderService.hide();
    },7000);
    //isCBadmin required for drilldown dashboard at Superadmin
    const isCBadminVal = this.authService.getRoles();
    if(isCBadminVal == '"ROLE_CBADMIN"'){
      this.isCBadmin = true;
    }else{
      this.isCBadmin = false;
    }

    this.orgadmin.getRolePerMissionCodes(this.authService.getRoles()).subscribe(
      (result:any) => {
      this.getRolePerMissionCodes = result.data;
        //console.log(this.getRolePerMissionCodes);
        if(this.getRolePerMissionCodes){
          if(this.getRolePerMissionCodes.includes('AGENTUPLOAD')){
            this.AGENTUPLOAD_stat = true;
          }

          if(this.getRolePerMissionCodes.includes('CANDIDATEUPLOAD')){
            this.CANDIDATEUPLOAD_stat = true;
          }

        }
    });


  }


  triggerModal(content: any) {
    this.modalService.open(content).result.then((res) => {
      this.closeModal = `Closed with: ${res}`;
    }, (res) => {
      this.closeModal = `Dismissed ${this.getDismissReason(res)}`;
    });
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return  `with: ${reason}`;
    }
  }

  toggleDisplayDiv() {
    this.isShowDiv = !this.isShowDiv;
  }

  getuserId(userId:any){
    if(userId != 'null'){
      localStorage.setItem('userId', userId);
      window.location.reload();
    }else{
      Swal.fire({
        title: 'Please select the user.',
        icon: 'success'
      });
    }
  }

  onfromDate(event:any) {
    let year = event.year;
    let month = event.month <= 9 ? '0' + event.month : event.month;;
    let day = event.day <= 9 ? '0' + event.day : event.day;
    let finalDate = day + "/" + month + "/" + year;
    this.fromDate = finalDate;
    this.getMinDate = { day:+day,month:+month,year:+year};
   }
   ontoDate(event:any) {
    let year = event.year;
    let month = event.month <= 9 ? '0' + event.month : event.month;;
    let day = event.day <= 9 ? '0' + event.day : event.day;;
    let finalDate = day + "/" + month + "/" + year;
    this.toDate = finalDate;
   }
   onSubmitFilter(dashboardFilter:FormGroup){
     let inputFromDate:any = $("#inputFromDate").val();
     //let getInputFromDate:any = inputFromDate.split('-');
     let finalInputFromDate = inputFromDate;

     let inputToDate:any = $("#inputToDate").val();
     //let getInputToDate:any = inputToDate.split('-');
     let finalInputToDate = inputToDate;

    if(this.fromDate==null){
        this.fromDate = finalInputFromDate;
    }
    if(this.toDate==null){
      this.toDate = finalInputToDate;
    }
    if (this.dashboardFilter.valid) {
      this.customer.setFromDate(this.fromDate);
      this.customer.setToDate(this.toDate);
      window.location.reload();
    }else{
      Swal.fire({
        title: 'Please select the valid dates.',
        icon: 'warning'
      });
    }
   }

   filterToday(){
    this.customer.setFromDate(this.initToday);
    this.customer.setToDate(this.initToday);
    window.location.reload();
  }

  filterLast7days(){
      var date = new Date();
      date.setDate(date.getDate() - 7);
      var dateString = date.toISOString().split('T')[0];
      let getInputFromDate:any = dateString.split('-');
      let finalInputFromDate = getInputFromDate[2] + "/" + getInputFromDate[1] + "/" + getInputFromDate[0];
      this.customer.setFromDate(finalInputFromDate);
      this.customer.setToDate(this.initToday);
      window.location.reload();
  }

  filterLast30days(){
    var date = new Date();
    date.setDate(date.getDate() - 30);
    var dateString = date.toISOString().split('T')[0];
    let getInputFromDate:any = dateString.split('-');
    let finalInputFromDate = getInputFromDate[2] + "/" + getInputFromDate[1] + "/" + getInputFromDate[0];
    this.customer.setFromDate(finalInputFromDate);
    this.customer.setToDate(this.initToday);
    window.location.reload();
}

filterByYear() {
  var date = new Date();
  date.setFullYear(date.getFullYear() - 1);  // subtract one year instead of 30 days
  var dateString = date.toISOString().split('T')[0];
  let getInputFromDate: any = dateString.split('-');
  let finalInputFromDate = getInputFromDate[2] + "/" + getInputFromDate[1] + "/" + getInputFromDate[0];
  this.customer.setFromDate(finalInputFromDate);
  this.customer.setToDate(this.initToday);
  window.location.reload();
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
  this.customer.setFromDate(finalInputFromDate);
  this.customer.setToDate(finalInputToDate);
  window.location.reload();
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

    this.customer.setFromDate(finalInputFromDate);
    this.customer.setToDate(this.initToday);
    window.location.reload();
}

}
